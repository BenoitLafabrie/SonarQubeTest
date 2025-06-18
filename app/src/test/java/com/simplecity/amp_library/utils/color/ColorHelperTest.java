package com.simplecity.amp_library.utils.color;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.Pair;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ColorHelperTest {

    private ColorHelper colorHelper;
    private Context mockContext;

    @Before
    public void setUp() {
        colorHelper = new ColorHelper();
        mockContext = mock(Context.class);
        // Mock getResources().getColor() for resolvePrimaryColor/resolveSecondaryColor
        android.content.res.Resources mockResources = mock(android.content.res.Resources.class);
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getColor(android.R.color.primary_text_light)).thenReturn(Color.WHITE);
        when(mockResources.getColor(android.R.color.primary_text_dark)).thenReturn(Color.BLACK);
        when(mockResources.getColor(android.R.color.secondary_text_light)).thenReturn(Color.LTGRAY);
        when(mockResources.getColor(android.R.color.secondary_text_dark)).thenReturn(Color.DKGRAY);
    }

    @Test
    public void testEnsureColorsWithoutForegroundLightBackground() {
        int backgroundColor = Color.WHITE;
        Pair<Integer, Integer> result = colorHelper.ensureColors(mockContext, false, backgroundColor, 0);
        assertNotNull(result);
        assertTrue(result.first == Color.WHITE || result.first == Color.BLACK);
        assertTrue(result.second == Color.LTGRAY || result.second == Color.DKGRAY);
    }

    @Test
    public void testEnsureColorsWithoutForegroundDarkBackground() {
        int backgroundColor = Color.BLACK;
        Pair<Integer, Integer> result = colorHelper.ensureColors(mockContext, false, backgroundColor, 0);
        assertNotNull(result);
        assertTrue(result.first == Color.WHITE || result.first == Color.BLACK);
        assertTrue(result.second == Color.LTGRAY || result.second == Color.DKGRAY);
    }

    @Test
    public void testEnsureColorsWithForegroundHighContrast() {
        int backgroundColor = Color.WHITE;
        int foregroundColor = Color.BLACK;
        Pair<Integer, Integer> result = colorHelper.ensureColors(mockContext, true, backgroundColor, foregroundColor);
        assertNotNull(result);
        assertEquals(foregroundColor, (int) result.first);
    }

    @Test
    public void testEnsureColorsWithForegroundLowContrast() {
        int backgroundColor = Color.WHITE;
        int foregroundColor = Color.LTGRAY;
        Pair<Integer, Integer> result = colorHelper.ensureColors(mockContext, true, backgroundColor, foregroundColor);
        assertNotNull(result);
        // Should not be the same as input foreground if contrast is low
        assertNotEquals(foregroundColor, (int) result.first);
    }

    @Test
    public void testIsColorLight() {
        assertTrue(ColorHelper.isColorLight(Color.WHITE));
        assertFalse(ColorHelper.isColorLight(Color.BLACK));
    }

    @Test
    public void testResolvePrimaryColor() {
        int color = ColorHelper.resolvePrimaryColor(mockContext, Color.WHITE);
        assertTrue(color == Color.WHITE || color == Color.BLACK);
    }

    @Test
    public void testResolveSecondaryColor() {
        int color = ColorHelper.resolveSecondaryColor(mockContext, Color.WHITE);
        assertTrue(color == Color.LTGRAY || color == Color.DKGRAY);
    }
}
