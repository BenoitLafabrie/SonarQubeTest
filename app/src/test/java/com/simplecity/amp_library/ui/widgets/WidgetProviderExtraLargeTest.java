package com.simplecity.amp_library.ui.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.RemoteViews;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.playback.MusicService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class WidgetProviderExtraLargeTest {

    private WidgetProviderExtraLarge widgetProvider;
    private Context mockContext;
    private SharedPreferences mockPrefs;
    private MusicService mockService;

    @Before
    @Before
    public void setUp() {
        widgetProvider = new WidgetProviderExtraLarge();
        mockContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        Resources mockResources = mock(Resources.class);
        mockService = mock(MusicService.class);

        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockContext.getPackageName()).thenReturn("com.simplecity.amp_library");
        when(mockService.getResources()).thenReturn(mockResources);
        when(mockService.getPackageName()).thenReturn("com.simplecity.amp_library");
    }
    @Test
    public void testGetUpdateCommandString() {
        String cmd = widgetProvider.getUpdateCommandString();
        assertEquals(WidgetProviderExtraLarge.CMDAPPWIDGETUPDATE, cmd);
    }

    @Test
    public void testGetLayoutIdString() {
        String layoutId = widgetProvider.getLayoutIdString();
        assertEquals(WidgetProviderExtraLarge.ARG_EXTRA_LARGE_LAYOUT_ID, layoutId);
    }

    @Test
    public void testGetWidgetLayoutId() {
        int layoutId = widgetProvider.getWidgetLayoutId();
        assertEquals(R.layout.widget_layout_extra_large, layoutId);
    }

    @Test
    public void testGetRootViewId() {
        int rootId = widgetProvider.getRootViewId();
        assertEquals(R.id.widget_layout_extra_large, rootId);
    }

    @Test
    public void testInitialiseWidget() {
        when(mockPrefs.getInt(anyString(), anyInt())).thenReturn(0);
        when(mockPrefs.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        widgetProvider.mLayoutId = R.layout.widget_layout_extra_large;
        widgetProvider.initialiseWidget(mockContext, mockPrefs, 1);
        // No exception means pass; for more, use ArgumentCaptor on RemoteViews
    }

    @Test
    public void testUpdate() {
        when(mockPrefs.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(mockPrefs.getInt(anyString(), anyInt())).thenReturn(R.layout.widget_layout_extra_large);
        Song song = new Song();
        song.name = "Test";
        song.albumName = "Album";
        song.albumArtistName = "Artist";
        when(mockService.getSong()).thenReturn(song);
        int[] ids = new int[]{1};
        widgetProvider.update(mockService, mockPrefs, ids, false);
        // No exception means pass; for more, use ArgumentCaptor on RemoteViews
    }
}
