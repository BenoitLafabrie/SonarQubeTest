package com.simplecity.amp_library.utils;

import android.content.Context;
import android.content.res.Resources;
import com.simplecity.amp_library.R;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StringUtilsTest {

    private static final String SONGS_LABEL = "5 songs";
    private Context mockContext;
    private static final String HELLO = "hello";

    @Before
    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        when(mockContext.getResources()).thenReturn(mockResources);

        // Mock string resources
        when(mockContext.getString(R.string.durationformatlong)).thenReturn("%1$s%2$d:%6$02d:%5$02d");
        when(mockContext.getString(R.string.durationformatshort)).thenReturn("%1$s%3$d:%6$02d");
        when(mockContext.getString(R.string.onefolder)).thenReturn("1 folder");
        when(mockContext.getString(R.string.onesong)).thenReturn("1 song");
        when(mockContext.getString(R.string.songs_time_label, SONGS_LABEL, "1:23:45")).thenReturn(SONGS_LABEL + " | 1:23:45");
        when(mockContext.getString(R.string.songs_time_label, SONGS_LABEL, "1:23:45")).thenReturn(SONGS_LABEL + " | 1:23:45");

        // Mock plurals
        when(mockResources.getQuantityText(R.plurals.Nfolders, 2)).thenReturn("%d folders");
        when(mockResources.getQuantityText(R.plurals.Nsongs, 5)).thenReturn("%d songs");
        when(mockResources.getQuantityText(R.plurals.Nalbums, 3)).thenReturn("%d albums");
    }
    @Test
    public void testMakeTimeStringShort() {
        String result = StringUtils.makeTimeString(mockContext, 59);
        assertTrue(result.contains("59"));
    }

    @Test
    public void testMakeTimeStringLong() {
        String result = StringUtils.makeTimeString(mockContext, 3661);
        assertTrue(result.contains("1:01:01"));
    }

    @Test
    public void testMakeSubfoldersLabel() {
        String label = StringUtils.makeSubfoldersLabel(mockContext, 2, 5);
        assertTrue(label.contains(SONGS_LABEL));
        assertTrue(label.contains(SONGS_LABEL));
    }

    @Test
    public void testMakeAlbumAndSongsLabel() {
        String label = StringUtils.makeAlbumAndSongsLabel(mockContext, 3, 5);
        assertTrue(label.contains("3 albums"));
        assertTrue(label.contains(SONGS_LABEL));
    }

    @Test
    public void testMakeAlbumsLabel() {
        String label = StringUtils.makeAlbumsLabel(mockContext, 3);
        assertTrue(label.contains("3 albums"));
    }

    @Test
    public void testMakeSongsLabel() {
        String label = StringUtils.makeSongsLabel(mockContext, 5);
        assertTrue(label.contains(SONGS_LABEL));
    }

    @Test
    public void testMakeYearLabelKnown() {
        String label = StringUtils.makeYearLabel(mockContext, 2020);
        assertEquals("2020", label);
    }

    @Test
    public void testMakeYearLabelUnknown() {
        String label = StringUtils.makeYearLabel(mockContext, 0);
        assertEquals("Unknown year", label);
    }

    @Test
    public void testMakeSongsAndTimeLabel() {
        String label = StringUtils.makeSongsAndTimeLabel(mockContext, 5, 5025);
        assertTrue(label.contains("songs"));
    }

    @Test
    public void testKeyFor() {
        assertEquals("beatles", StringUtils.keyFor("The Beatles"));
        assertEquals("queen", StringUtils.keyFor("Queen"));
        assertEquals("", StringUtils.keyFor(""));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(StringUtils.containsIgnoreCase("Hello World", "world"));
        assertFalse(StringUtils.containsIgnoreCase("Hello", "bye"));
    }
    @Test
    public void testGetAdjustedJaroWinklerSimilarity() {
        double sim = StringUtils.getAdjustedJaroWinklerSimilarity("hello world", HELLO);
        assertTrue(sim > 0.7);
        assertEquals(0, StringUtils.getAdjustedJaroWinklerSimilarity(null, "test"), 0.0);
    }

    @Test
    public void testGetJaroWinklerSimilarity() {
        double sim = StringUtils.getJaroWinklerSimilarity(HELLO, HELLO);
        assertEquals(1.0, sim, 0.01);
        double sim2 = StringUtils.getJaroWinklerSimilarity(HELLO, "hallo");
        assertTrue(sim2 < 1.0 && sim2 > 0.7);
    }
    }

    @Test
    public void testParseInt() {
        assertEquals(123, StringUtils.parseInt("123"));
        assertEquals(-1, StringUtils.parseInt("abc"));
        assertEquals(-1, StringUtils.parseInt(null));
    }
}
