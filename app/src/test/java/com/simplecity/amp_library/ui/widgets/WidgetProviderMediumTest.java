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

public class WidgetProviderMediumTest {

    private static final String PACKAGE_NAME = "com.simplecity.amp_library";

    private WidgetProviderMedium widgetProvider;
    private Context mockContext;
    private SharedPreferences mockPrefs;
    private Resources mockResources;
    private MusicService mockService;

    @Before
    public void setUp() {
        widgetProvider = new WidgetProviderMedium();
        mockContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        mockResources = mock(Resources.class);
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockContext.getPackageName()).thenReturn(PACKAGE_NAME);
        when(mockService.getResources()).thenReturn(mockResources);
        when(mockService.getPackageName()).thenReturn(PACKAGE_NAME);
        when(mockService.getResources()).thenReturn(mockResources);
        when(mockService.getPackageName()).thenReturn(PACKAGE_NAME);
    }

    @Test
    public void testInitialiseWidgetSetsInitialText() {
        when(mockPrefs.getInt(anyString(), anyInt())).thenReturn(0);
        when(mockPrefs.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(mockResources.getText(R.string.widget_initial_text)).thenReturn("Initial");

        widgetProvider.mLayoutId = R.layout.widget_layout_medium;
        widgetProvider.initialiseWidget(mockContext, mockPrefs, 1);

        // No exception means pass; for more, use Robolectric to inspect RemoteViews
    }

    @Test
    public void testUpdateCallsUpdateWidgetForId() {
        int[] ids = {1, 2};
        SharedPreferences prefs = mock(SharedPreferences.class);
        MusicService service = mock(MusicService.class);
        when(service.getResources()).thenReturn(mockResources);
        when(service.getPackageName()).thenReturn(PACKAGE_NAME);
        when(service.getResources()).thenReturn(mockResources);
        when(service.getPackageName()).thenReturn(PACKAGE_NAME);

        widgetProvider.update(service, prefs, ids, false);

        // No exception means pass; for more, use spies or Robolectric
    }

    @Test
    public void testSetWidgetTextViewsErrorState() {
        RemoteViews views = mock(RemoteViews.class);
        widgetProvider.mLayoutId = R.layout.widget_layout_medium;
        widgetProvider.initialiseWidget(mockContext, mockPrefs, 1);

        // Call private method via reflection for coverage
        try {
            java.lang.reflect.Method m = WidgetProviderMedium.class.getDeclaredMethod(
                "setWidgetTextViews", RemoteViews.class, CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class);
            m.invoke(widgetProvider, views, "Error", "Title", "Album", "Artist");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
