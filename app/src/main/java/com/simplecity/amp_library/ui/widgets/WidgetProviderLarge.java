package com.simplecity.amp_library.ui.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.playback.MusicService;
import com.simplecity.amp_library.utils.ColorUtils;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WidgetProviderLarge extends BaseWidgetProvider {

    public static final String ARG_LARGE_LAYOUT_ID = "widget_large_layout_id_";
    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate_large";

    @Inject
    public WidgetProviderLarge() {
        // This constructor is intentionally empty because dependency injection is handled by the framework.
    }

    @Override
    public String getUpdateCommandString() {
        return CMDAPPWIDGETUPDATE;
    }

    @Override
    public String getLayoutIdString() {
        return ARG_LARGE_LAYOUT_ID;
    }

    @Override
    public int getWidgetLayoutId() {
        return R.layout.widget_layout_large;
    }

    @Override
    public int getRootViewId() {
        return R.id.widget_layout_large;
    }

    protected void initialiseWidget(Context context, SharedPreferences sharedPreferences, int appWidgetId) {
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), mLayoutId);

        views.setViewVisibility(R.id.text1, View.GONE);
        views.setTextViewText(R.id.text2, res.getText(R.string.widget_initial_text));

        int textColor = sharedPreferences.getInt(ARG_WIDGET_TEXT_COLOR + appWidgetId, ContextCompat.getColor(context, R.color.white));
        views.setImageViewResource(R.id.next_button, R.drawable.ic_skip_next_24dp);
        views.setImageViewResource(R.id.prev_button, R.drawable.ic_skip_previous_24dp);
        views.setTextColor(R.id.text3, textColor);
        views.setTextColor(R.id.text2, textColor);
        views.setTextColor(R.id.text1, textColor);

        int backgroundColor = sharedPreferences.getInt(ARG_WIDGET_BACKGROUND_COLOR + appWidgetId, ColorUtils.adjustAlpha(ContextCompat.getColor(context, R.color.white), 35 / 255f));
        views.setInt(R.id.widget_layout_large, "setBackgroundColor", backgroundColor);
        int colorFilter = sharedPreferences.getInt(ARG_WIDGET_COLOR_FILTER + appWidgetId, -1);
        if (colorFilter != -1) {
            views.setInt(R.id.album_art, "setColorFilter", colorFilter);
        }
        boolean showAlbumArt = sharedPreferences.getBoolean(ARG_WIDGET_SHOW_ARTWORK + appWidgetId, true);
        if (!showAlbumArt) {
            views.setViewVisibility(R.id.album_art, View.GONE);
        }

        setupButtons(context, views, appWidgetId, getRootViewId());
        pushUpdate(context, appWidgetId, views);
    }

    public void update(MusicService service, SharedPreferences sharedPreferences, int[] appWidgetIds, boolean updateArtwork) {
        if (appWidgetIds == null) return;
        for (int appWidgetId : appWidgetIds) {
            updateWidgetForId(service, sharedPreferences, appWidgetIds, appWidgetId, updateArtwork);
        }
    }

    private void updateWidgetForId(MusicService service, SharedPreferences sharedPreferences, int[] appWidgetIds, int appWidgetId, boolean updateArtwork) {
        boolean showAlbumArt = sharedPreferences.getBoolean(ARG_WIDGET_SHOW_ARTWORK + appWidgetId, true);
        mLayoutId = sharedPreferences.getInt(ARG_LARGE_LAYOUT_ID + appWidgetId, R.layout.widget_layout_large);
        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), mLayoutId);

        Song song = service.getSong();
        CharSequence titleName = song != null ? song.name : "";
        CharSequence albumName = song != null ? song.albumName : "";
        CharSequence artistName = song != null ? song.albumArtistName : "";

        CharSequence errorState = getErrorState(res, titleName);

        setWidgetTextViews(views, errorState, titleName, albumName, artistName);

        boolean invertIcons = sharedPreferences.getBoolean(ARG_WIDGET_INVERT_ICONS + appWidgetId, false);
        setPlayPauseButton(service, views, invertIcons, R.id.play_button);

        setupShuffleView(service, views, invertIcons);
        setupRepeatView(service, views, invertIcons);

        int textColor = sharedPreferences.getInt(ARG_WIDGET_TEXT_COLOR + appWidgetId, ContextCompat.getColor(service, R.color.white));
        setNavigationButtons(service, views, invertIcons, R.id.next_button, R.id.prev_button);

        views.setTextColor(R.id.text3, textColor);
        views.setTextColor(R.id.text2, textColor);
        views.setTextColor(R.id.text1, textColor);

        int backgroundColor = sharedPreferences.getInt(ARG_WIDGET_BACKGROUND_COLOR + appWidgetId, ColorUtils.adjustAlpha(ContextCompat.getColor(service, R.color.white), 35 / 255f));
        views.setInt(R.id.widget_layout_large, "setBackgroundColor", backgroundColor);

        setupButtons(service, views, appWidgetId, getRootViewId());

        if (!showAlbumArt) {
            views.setViewVisibility(R.id.album_art, View.GONE);
        }

        pushUpdate(service, appWidgetId, views);

        if (updateArtwork && errorState == null && showAlbumArt) {
            updateArtwork(service, sharedPreferences, appWidgetIds, appWidgetId, views);
        }
    }

    private void setWidgetTextViews(RemoteViews views, CharSequence errorState, CharSequence titleName, CharSequence albumName, CharSequence artistName) {
        if (errorState != null) {
            views.setViewVisibility(R.id.text1, View.GONE);
            views.setTextViewText(R.id.text2, errorState);
        } else {
            views.setViewVisibility(R.id.text1, View.VISIBLE);
            views.setTextViewText(R.id.text1, titleName);
            views.setTextViewText(R.id.text2, albumName);
            views.setTextViewText(R.id.text3, artistName);
        }
    }

    private void updateArtwork(MusicService service, SharedPreferences sharedPreferences, int[] appWidgetIds, int appWidgetId, RemoteViews views) {
        views.setImageViewResource(R.id.album_art, R.drawable.ic_placeholder_light_medium);
        int colorFilter = sharedPreferences.getInt(ARG_WIDGET_COLOR_FILTER + appWidgetId, -1);
        if (colorFilter != -1) {
            views.setInt(R.id.album_art, "setColorFilter", colorFilter);
        }
        doOnMainThread(() -> loadArtwork(service, appWidgetIds, views, 512));
    }
}