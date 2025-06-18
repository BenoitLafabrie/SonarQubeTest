package com.simplecity.amp_library.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.Stream;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.model.ArtworkModel;
import com.simplecity.amp_library.model.ArtworkProvider;
import com.simplecity.amp_library.model.UserSelectedArtwork;
import com.simplecity.amp_library.sql.databases.CustomArtworkTable;
import com.simplecity.amp_library.ui.modelviews.ArtworkLoadingView;
import com.simplecity.amp_library.ui.modelviews.ArtworkView;
import com.simplecity.amp_library.ui.views.recyclerview.SpacesItemDecoration;
import com.simplecityapps.recycler_adapter.adapter.ViewModelAdapter;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import com.simplecityapps.recycler_adapter.recyclerview.RecyclerListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArtworkDialog {

    private static final String TAG = "ArtworkDialog";

    private ArtworkDialog() {

    }

    public static MaterialDialog build(Context context, ArtworkProvider artworkProvider) {

        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_artwork, null);

        ViewModelAdapter adapter = new ViewModelAdapter();
        RecyclerView recyclerView = setupRecyclerView(context, customView, adapter);

        ArtworkView.GlideListener glideListener = getGlideListener(adapter);

        List<ViewModel> viewModels = buildInitialViewModels(context, artworkProvider, glideListener);

        ArtworkView folderView = addDummyFolderView(viewModels);

        ArtworkView.ClickListener listener = getArtworkClickListener(viewModels, adapter);

        setListenersOnViewModels(viewModels, listener);

        adapter.setItems(viewModels);

        UserSelectedArtwork userSelectedArtwork = ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.get(artworkProvider.getArtworkKey());

        loadFolderArtworkFilesAsync(artworkProvider, adapter, folderView, userSelectedArtwork, glideListener);

        return buildMaterialDialog(context, artworkProvider, customView, adapter, recyclerView, glideListener);
    }

    private static RecyclerView setupRecyclerView(Context context, View customView, ViewModelAdapter adapter) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = customView.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SpacesItemDecoration(16));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(0);
        recyclerView.setRecyclerListener(new RecyclerListener());

        adapter.items.add(0, new ArtworkLoadingView());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    private static ArtworkView.GlideListener getGlideListener(ViewModelAdapter adapter) {
        return artworkView -> {
            int index = adapter.items.indexOf(artworkView);
            if (index != -1) {
                adapter.removeItem(index);
            }
        };
    }

    private static List<ViewModel> buildInitialViewModels(Context context, ArtworkProvider artworkProvider, ArtworkView.GlideListener glideListener) {
        List<ViewModel> viewModels = new ArrayList<>();
        UserSelectedArtwork userSelectedArtwork = ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.get(artworkProvider.getArtworkKey());
        if (userSelectedArtwork != null) {
            File file = null;
            if (userSelectedArtwork.path != null) {
                file = new File(userSelectedArtwork.path);
            }
            ArtworkView artworkView = new ArtworkView(userSelectedArtwork.type, artworkProvider, glideListener, file, true);
            artworkView.setSelected(true);
            viewModels.add(artworkView);
        }

        if (userSelectedArtwork == null || userSelectedArtwork.type != ArtworkProvider.Type.MEDIA_STORE) {
            viewModels.add(new ArtworkView(ArtworkProvider.Type.MEDIA_STORE, artworkProvider, glideListener));
        }
        if (userSelectedArtwork == null || userSelectedArtwork.type != ArtworkProvider.Type.TAG) {
            viewModels.add(new ArtworkView(ArtworkProvider.Type.TAG, artworkProvider, glideListener));
        }
        if (userSelectedArtwork == null || userSelectedArtwork.type != ArtworkProvider.Type.REMOTE) {
            viewModels.add(new ArtworkView(ArtworkProvider.Type.REMOTE, artworkProvider, glideListener));
        }
        return viewModels;
    }

    private static ArtworkView addDummyFolderView(List<ViewModel> viewModels) {
        ArtworkView folderView = new ArtworkView(ArtworkProvider.Type.FOLDER, null, null);
        viewModels.add(folderView);
        return folderView;
    }

    private static ArtworkView.ClickListener getArtworkClickListener(List<ViewModel> viewModels, ViewModelAdapter adapter) {
        return artworkView -> {
            Stream.of(viewModels)
                    .filter(ArtworkView.class::isInstance)
                    .forEachIndexed((i, viewModel) -> ((ArtworkView) viewModel).setSelected(viewModel == artworkView));
            adapter.notifyItemRangeChanged(0, adapter.getItemCount(), 0);
        };
    }

    private static void setListenersOnViewModels(List<ViewModel> viewModels, ArtworkView.ClickListener listener) {
        Stream.of(viewModels)
                .filter(ArtworkView.class::isInstance)
                .forEach(viewModel -> ((ArtworkView) viewModel).setListener(listener));
    }

    private static void loadFolderArtworkFilesAsync(ArtworkProvider artworkProvider, ViewModelAdapter adapter, ArtworkView folderView, UserSelectedArtwork userSelectedArtwork, ArtworkView.GlideListener glideListener) {
        Observable.fromCallable(artworkProvider::getFolderArtworkFiles)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(files -> {
                    adapter.removeItem(adapter.items.indexOf(folderView));
                    if (files != null) {
                        Stream.of(files)
                                .filter(file -> userSelectedArtwork == null || !file.getPath().equals(userSelectedArtwork.path))
                                .forEach(file ->
                                        adapter.addItem(new ArtworkView(ArtworkProvider.Type.FOLDER, artworkProvider, glideListener, file, false)));
                    }
                }, error -> LogUtils.logException(TAG, "Error getting artwork files", error));
    }

    private static MaterialDialog buildMaterialDialog(Context context, ArtworkProvider artworkProvider, View customView, ViewModelAdapter adapter, RecyclerView recyclerView, ArtworkView.GlideListener glideListener) {
        return new MaterialDialog.Builder(context)
                .title(R.string.artwork_edit)
                .customView(customView, false)
                .autoDismiss(false)
                .positiveText(context.getString(R.string.save))
                .onPositive((dialog, which) -> handlePositiveClick(context, artworkProvider, adapter, dialog))
                .negativeText(context.getString(R.string.close))
                .onNegative((dialog, which) -> dialog.dismiss())
                .neutralText(context.getString(R.string.artwork_gallery))
                .onNeutral((dialog, which) -> handleGalleryPick(context, artworkProvider, adapter, recyclerView, glideListener))
                .cancelable(false)
                .build();
    }

    private static void handlePositiveClick(Context context, ArtworkProvider artworkProvider, ViewModelAdapter adapter, MaterialDialog dialog) {
        ArtworkView checkedView = ArtworkDialog.getCheckedView(adapter.items);
        if (checkedView != null) {
            ArtworkModel artworkModel = checkedView.getItem();
            ContentValues values = new ContentValues();
            values.put(CustomArtworkTable.COLUMN_KEY, artworkProvider.getArtworkKey());
            values.put(CustomArtworkTable.COLUMN_TYPE, artworkModel.type);
            values.put(CustomArtworkTable.COLUMN_PATH, artworkModel.file == null ? null : artworkModel.file.getPath());
            context.getContentResolver().insert(CustomArtworkTable.URI, values);

            ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.put(artworkProvider.getArtworkKey(),
                    new UserSelectedArtwork(artworkModel.type, artworkModel.file == null ? null : artworkModel.file.getPath()));
        } else {
            context.getContentResolver().delete(CustomArtworkTable.URI, CustomArtworkTable.COLUMN_KEY + "='" + artworkProvider.getArtworkKey().replaceAll("'", "\''") + "'", null);
            ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.remove(artworkProvider.getArtworkKey());
        }
        dialog.dismiss();
    }

    private static void handleGalleryPick(Context context, ArtworkProvider artworkProvider, ViewModelAdapter adapter, RecyclerView recyclerView, ArtworkView.GlideListener glideListener) {
        RxImagePicker.with(context)
                .requestImage(Sources.GALLERY)
                .flatMap(uri -> prepareArtworkFile(context, artworkProvider, uri))
                .filter(file -> file != null && file.exists())
                .subscribe(
                        file -> updateAdapterWithArtworkFile(adapter, artworkProvider, glideListener, file, recyclerView),
                        error -> LogUtils.logException(TAG, "Error picking from gallery", error)
                );
    }

    private static Observable<File> prepareArtworkFile(Context context, ArtworkProvider artworkProvider, android.net.Uri uri) {
        File dir = getOrCreateArtworkDir(context, artworkProvider);
        clearDirectory(dir);
        File file = new File(dir.getPath() + System.currentTimeMillis() + ".artwork");
        // Debug feature: file creation and stack trace printing
        // Make sure this debug feature is deactivated before delivering the code in production.
        try {
            boolean created = file.createNewFile();
            if (created && file.exists()) {
            return RxImageConverters.uriToFile(context, uri, file);
            } else if (!created) {
            LogUtils.logException(TAG, "Failed to create new artwork file", null);
            }
        } catch (IOException e) {
            // e.printStackTrace(); // Debug only. Deactivate or remove before production.
        }
        return Observable.just(null);
    }

    private static File getOrCreateArtworkDir(Context context, ArtworkProvider artworkProvider) {
        File dir = new File(context.getFilesDir() + File.separator + "shuttle" + File.separator + "custom_artwork" + File.separator + artworkProvider.getArtworkKey().hashCode() + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static void clearDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    File file = new File(dir, child);
                    try {
                        java.nio.file.Files.delete(file.toPath());
                    } catch (IOException e) {
                        LogUtils.logException(TAG, "Failed to delete file: " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
    }

    private static void updateAdapterWithArtworkFile(ViewModelAdapter adapter, ArtworkProvider artworkProvider, ArtworkView.GlideListener glideListener, File file, RecyclerView recyclerView) {
        if (adapter.getItemCount() != 0) {
            File aFile = ((ArtworkView) adapter.items.get(0)).file;
            if (aFile != null && aFile.getPath().contains(artworkProvider.getArtworkKey())) {
                adapter.removeItem(0);
            }
        }
        ArtworkView artworkView = new ArtworkView(ArtworkProvider.Type.FOLDER, artworkProvider, glideListener, file, true);
        artworkView.setSelected(true);
        adapter.addItem(0, artworkView);
        recyclerView.scrollToPosition(0);
    }

    @Nullable
    public static ArtworkView getCheckedView(List<ViewModel> viewModels) {
        return (ArtworkView) Stream.of(viewModels)
                .filter(viewModel -> viewModel instanceof ArtworkView && ((ArtworkView) viewModel).isSelected())
                .findFirst()
                .orElse(null);
    }
}