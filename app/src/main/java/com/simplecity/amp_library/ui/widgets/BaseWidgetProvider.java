package com.simplecity.amp_library.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.glide.utils.CustomAppWidgetTarget;
import com.simplecity.amp_library.playback.MusicService;
import com.simplecity.amp_library.playback.QueueManager;
import com.simplecity.amp_library.playback.constants.InternalIntents;
import com.simplecity.amp_library.playback.constants.MediaButtonCommand;
import com.simplecity.amp_library.playback.constants.ServiceCommand;
import com.simplecity.amp_library.rx.UnsafeAction;
import com.simplecity.amp_library.ui.screens.main.MainActivity;
import com.simplecity.amp_library.utils.DrawableUtils;
import com.simplecity.amp_library.utils.ShuttleUtils;

public abstract class BaseWidgetProvider extends AppWidgetProvider {

    public abstract String getUpdateCommandString();

    public abstract String getLayoutIdString();

    public abstract int getWidgetLayoutId();

    public abstract int getRootViewId();

    protected void doOnMainThread(UnsafeAction action) {
        new Handler(Looper.getMainLooper()).post(action::run);
    }

    public static final String ARG_WIDGET_BACKGROUND_COLOR = "widget_background_color_";
    public static final String ARG_WIDGET_TEXT_COLOR = "widget_text_color_";
    public static final String ARG_WIDGET_INVERT_ICONS = "widget_invert_icons_";
    public static final String ARG_WIDGET_SHOW_ARTWORK = "widget_show_artwork_";
    public static final String ARG_WIDGET_COLOR_FILTER = "widget_color_filter_";

    @LayoutRes
    public int mLayoutId;

    public abstract void update(MusicService service, SharedPreferences sharedPreferences, int[] appWidgetIds, boolean updateArtwork);

    protected abstract void initialiseWidget(Context context, SharedPreferences sharedPreferences, int appWidgetId);

    SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        SharedPreferences sharedPreferences = getSharedPreferences(context);

        for (int appWidgetId : appWidgetIds) {
            mLayoutId = sharedPreferences.getInt(getLayoutIdString() + appWidgetId, getWidgetLayoutId());
            initialiseWidget(context, sharedPreferences, appWidgetId);
        }

        // Send broadcast intent to any running MusicService so it can wrap around with an immediate update.
        Intent updateIntent = new Intent(ServiceCommand.COMMAND);
        updateIntent.putExtra(MediaButtonCommand.CMD_NAME, getUpdateCommandString());
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    protected void pushUpdate(Context context, int appWidgetId, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (appWidgetId != -1) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }

    private int[] getInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return (appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass())));
    }

    public void notifyChange(MusicService service, String what) {
        if (getInstances(service) != null) {
            if (InternalIntents.META_CHANGED.equals(what)
                    || InternalIntents.PLAY_STATE_CHANGED.equals(what)
                    || InternalIntents.SHUFFLE_CHANGED.equals(what)
                    || InternalIntents.REPEAT_CHANGED.equals(what)) {
                update(service, getSharedPreferences(service), getInstances(service), InternalIntents.META_CHANGED.equals(what));
            }
        }
    }

    public static void setupButtons(Context context, RemoteViews views, int appWidgetId, int rootViewId) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        views.setOnClickPendingIntent(rootViewId, pendingIntent);

        pendingIntent = getPendingIntent(context, appWidgetId, new Intent(ServiceCommand.TOGGLE_PLAYBACK));
        views.setOnClickPendingIntent(R.id.play_button, pendingIntent);

        pendingIntent = getPendingIntent(context, appWidgetId, new Intent(ServiceCommand.NEXT));
        views.setOnClickPendingIntent(R.id.next_button, pendingIntent);

        pendingIntent = getPendingIntent(context, appWidgetId, new Intent(ServiceCommand.PREV));
        views.setOnClickPendingIntent(R.id.prev_button, pendingIntent);

        pendingIntent = getPendingIntent(context, appWidgetId, new Intent(ServiceCommand.SHUFFLE));
        views.setOnClickPendingIntent(R.id.shuffle_button, pendingIntent);

        pendingIntent = getPendingIntent(context, appWidgetId, new Intent(ServiceCommand.REPEAT));
        views.setOnClickPendingIntent(R.id.repeat_button, pendingIntent);
    }

    private static PendingIntent getPendingIntent(Context context, int appWidgetId, Intent intent) {
        intent.setComponent(new ComponentName(context, MusicService.class));
        if (ShuttleUtils.hasOreo()) {
            return PendingIntent.getForegroundService(context, appWidgetId, intent, 0);
        } else {
            return PendingIntent.getService(context, appWidgetId, intent, 0);
        }
    }

    void loadArtwork(MusicService service, int[] appWidgetIds, RemoteViews views, int bitmapSize) {
        //Try to load the artwork. If it fails, halve the dimensions and try again.
        loadArtwork(service, views, bitmapSize, e ->
                loadArtwork(service, views, bitmapSize / 2, e1 ->
                        //If this one doesn't work, load a placeholder.
                        loadArtwork(service, views, bitmapSize / 3, e2
                                        -> views.setImageViewResource(R.id.album_art, R.drawable.ic_placeholder_light_medium),
                                appWidgetIds), appWidgetIds);
    }

    void loadArtwork(MusicService service, RemoteViews views, int size, CustomAppWidgetTarget.CustomErrorListener errorListener, int... appWidgetIds) {
        Glide.with(service)
                .load(service.getSong())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomAppWidgetTarget(service, views, R.id.album_art, size, size, errorListener, appWidgetIds));
    }

    void setupRepeatView(MusicService service, RemoteViews views, boolean invertIcons) {
        switch (service.getRepeatMode()) {
            case QueueManager.RepeatMode.ALL:
                views.setImageViewBitmap(R.id.repeat_button, DrawableUtils.getColoredBitmap(service, R.drawable.ic_repeat_24dp_scaled));
                views.setContentDescription(R.id.shuffle_button, service.getString(R.string.btn_repeat_current));
                break;
            case QueueManager.RepeatMode.ONE:
                views.setImageViewBitmap(R.id.repeat_button, DrawableUtils.getColoredBitmap(service, R.drawable.ic_repeat_one_24dp_scaled));
                views.setContentDescription(R.id.shuffle_button, service.getString(R.string.btn_repeat_off));
                break;
            default:
                if (invertIcons) {
                    views.setImageViewBitmap(R.id.repeat_button, DrawableUtils.getBlackBitmap(service, R.drawable.ic_repeat_24dp_scaled));
                } else {
                    views.setImageViewResource(R.id.repeat_button, R.drawable.ic_repeat_24dp_scaled);
                }
                views.setContentDescription(R.id.shuffle_button, service.getString(R.string.btn_repeat_all));
                break;
        }
    }

    void setupShuffleView(MusicService service, RemoteViews views, boolean invertIcons) {
        switch (service.getShuffleMode()) {
            case QueueManager.ShuffleMode.OFF:
                if (invertIcons) {
                    views.setImageViewBitmap(R.id.shuffle_button, DrawableUtils.getBlackBitmap(service, R.drawable.ic_shuffle_24dp_scaled));
                } else {
                    views.setImageViewResource(R.id.shuffle_button, R.drawable.ic_shuffle_24dp_scaled);
                }
                views.setContentDescription(R.id.shuffle_button, service.getString(R.string.btn_shuffle_on));
                break;
            default:
                views.setImageViewBitmap(R.id.shuffle_button, DrawableUtils.getColoredBitmap(service, R.drawable.ic_shuffle_24dp_scaled));
                views.setContentDescription(R.id.shuffle_button, service.getString(R.string.btn_shuffle_off));
                break;
        }
    }

    protected CharSequence getErrorState(Resources res, CharSequence titleName) {
        String status = android.os.Environment.getExternalStorageState();
        if (status.equals(android.os.Environment.MEDIA_SHARED) || status.equals(android.os.Environment.MEDIA_UNMOUNTED)) {
            if (android.os.Environment.isExternalStorageRemovable()) {
                return res.getText(R.string.sdcard_busy_title);
            } else {
                return res.getText(R.string.sdcard_busy_title_nosdcard);
            }
        } else if (status.equals(android.os.Environment.MEDIA_REMOVED)) {
            if (android.os.Environment.isExternalStorageRemovable()) {
                return res.getText(R.string.sdcard_missing_title);
            } else {
                return res.getText(R.string.sdcard_missing_title_nosdcard);
            }
        } else if (titleName == null) {
            return res.getText(R.string.emptyplaylist);
        }
        return null;
    }

    protected void setPlayPauseButton(MusicService service, RemoteViews views, boolean invertIcons, int playButtonId) {
        final boolean isPlaying = service.isPlaying();
        if (isPlaying) {
            if (invertIcons) {
                views.setImageViewBitmap(playButtonId, DrawableUtils.getBlackBitmap(service, R.drawable.ic_pause_24dp));
            } else {
                views.setImageViewResource(playButtonId, R.drawable.ic_pause_24dp);
            }
        } else {
            if (invertIcons) {
                views.setImageViewBitmap(playButtonId, DrawableUtils.getBlackBitmap(service, R.drawable.ic_play_24dp));
            } else {
                views.setImageViewResource(playButtonId, R.drawable.ic_play_24dp);
            }
        }
    }

    protected void setNavigationButtons(MusicService service, RemoteViews views, boolean invertIcons, int nextButtonId, int prevButtonId) {
        if (invertIcons) {
            views.setImageViewBitmap(nextButtonId, DrawableUtils.getBlackBitmap(service, R.drawable.ic_skip_next_24dp));
            views.setImageViewBitmap(prevButtonId, DrawableUtils.getBlackBitmap(service, R.drawable.ic_skip_previous_24dp));
        } else {
            views.setImageViewResource(nextButtonId, R.drawable.ic_skip_next_24dp);
            views.setImageViewResource(prevButtonId, R.drawable.ic_skip_previous_24dp);
        }
    }
}
