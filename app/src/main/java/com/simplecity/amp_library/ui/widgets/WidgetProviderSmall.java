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
public class WidgetProviderSmall extends BaseWidgetProvider {

    public static final String ARG_SMALL_LAYOUT_ID = "widget_small_layout_id_";
    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate_small";

    @Inject
    public WidgetProviderSmall() {
        // Constructor is intentionally empty because dependency injection is used.
    }

    @Override
    public String getUpdateCommandString() {
        return CMDAPPWIDGETUPDATE;
    }

    @Override
    public String getLayoutIdString() {
        return ARG_SMALL_LAYOUT_ID;
    }

    @Override
    public int getWidgetLayoutId() {
        return R.layout.widget_layout_small;
    }

    @Override
    public int getRootViewId() {
        return R.id.widget_layout_small;
    }

    protected void initialiseWidget(Context context, SharedPreferences sharedPreferences, int appWidgetId) {
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), mLayoutId);

        views.setViewVisibility(R.id.text1, View.GONE);
        views.setTextViewText(R.id.text2, res.getText(R.string.widget_initial_text));

        int textColor = sharedPreferences.getInt(ARG_WIDGET_TEXT_COLOR + appWidgetId, ContextCompat.getColor(context, R.color.white));
        views.setImageViewResource(R.id.next_button, R.drawable.ic_skip_next_24dp);
        views.setImageViewResource(R.id.prev_button, R.drawable.ic_skip_previous_24dp);
        views.setTextColor(R.id.text2, textColor);
        views.setTextColor(R.id.text1, textColor);

        int backgroundColor = sharedPreferences.getInt(ARG_WIDGET_BACKGROUND_COLOR + appWidgetId, ColorUtils.adjustAlpha(ContextCompat.getColor(context, R.color.white), 35 / 255f));
        views.setInt(R.id.widget_layout_small, "setBackgroundColor", backgroundColor);
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
            updateSingleWidget(service, sharedPreferences, appWidgetId);
        }
    }

    private void updateSingleWidget(MusicService service, SharedPreferences sharedPreferences, int appWidgetId) {
        boolean showAlbumArt = sharedPreferences.getBoolean(ARG_WIDGET_SHOW_ARTWORK + appWidgetId, true);
        mLayoutId = sharedPreferences.getInt(ARG_SMALL_LAYOUT_ID + appWidgetId, R.layout.widget_layout_small);

        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), mLayoutId);

        Song song = service.getSong();
        CharSequence titleName = (song != null) ? song.name : "";
        CharSequence artistName = (song != null) ? song.albumArtistName : "";

        CharSequence errorState = getErrorState(res, titleName);

        setTitleAndArtistViews(views, errorState, titleName, artistName);

        boolean invertIcons = sharedPreferences.getBoolean(ARG_WIDGET_INVERT_ICONS + appWidgetId, false);

        setPlayPauseButton(service, views, invertIcons, R.id.play_button);

        setupShuffleView(service, views, invertIcons);
        setupRepeatView(service, views, invertIcons);

        int textColor = sharedPreferences.getInt(ARG_WIDGET_TEXT_COLOR + appWidgetId, ContextCompat.getColor(service, R.color.white));
        setNavigationButtons(service, views, invertIcons, R.id.next_button, R.id.prev_button);

        views.setTextColor(R.id.text2, textColor);
        views.setTextColor(R.id.text1, textColor);

        int backgroundColor = sharedPreferences.getInt(ARG_WIDGET_BACKGROUND_COLOR + appWidgetId, ColorUtils.adjustAlpha(ContextCompat.getColor(service, R.color.white), 35 / 255f));
        views.setInt(R.id.widget_layout_small, "setBackgroundColor", backgroundColor);

        setupButtons(service, views, appWidgetId, getRootViewId());

        if (!showAlbumArt) {
            views.setViewVisibility(R.id.album_art, View.GONE);
        }

        views.setImageViewResource(R.id.album_art, R.drawable.ic_placeholder_light_medium);

        pushUpdate(service, appWidgetId, views);
    }

    private void setTitleAndArtistViews(RemoteViews views, CharSequence errorState, CharSequence titleName, CharSequence artistName) {
        if (errorState != null) {
            views.setViewVisibility(R.id.text1, View.GONE);
            views.setTextViewText(R.id.text2, errorState);
        } else {
            views.setViewVisibility(R.id.text1, View.VISIBLE);
            views.setTextViewText(R.id.text1, titleName);
            views.setTextViewText(R.id.text2, artistName);
        }
    }
}