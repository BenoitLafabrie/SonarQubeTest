package android.support.design.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CustomCollapsingToolbarLayoutTest {

    private Context mockContext;
    private AttributeSet mockAttrs;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockAttrs = mock(AttributeSet.class);
    }

    @Test
    public void testConstructors() {
        new CustomCollapsingToolbarLayout(mockContext);
        new CustomCollapsingToolbarLayout(mockContext, mockAttrs);
        new CustomCollapsingToolbarLayout(mockContext, mockAttrs, 0);
    }

    @Test
    public void testSetAndGetTitle() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setTitle("Test Title");
        assertEquals("Test Title", layout.getTitle());
    }

    @Test
    public void testSetAndGetSubtitle() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setSubtitle("Test Subtitle");
        // No getter, but should not throw
    }

    @Test
    public void testSetTitleEnabled() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setTitleEnabled(false);
        assertFalse(layout.isTitleEnabled());
        layout.setTitleEnabled(true);
        assertTrue(layout.isTitleEnabled());
    }

    @Test
    public void testSetContentScrimColor() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setContentScrimColor(Color.RED);
        assertTrue(layout.getContentScrim() instanceof ColorDrawable);
    }

    @Test
    public void testSetStatusBarScrimColor() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setStatusBarScrimColor(Color.BLUE);
        assertTrue(layout.getStatusBarScrim() instanceof ColorDrawable);
    }

    @Test
    public void testSetScrimsShown() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setScrimsShown(true);
        layout.setScrimsShown(false);
        // Should not throw
    }

    @Test
    public void testSetScrimAnimationDuration() {
        CustomCollapsingToolbarLayout layout = new CustomCollapsingToolbarLayout(mockContext);
        layout.setScrimAnimationDuration(1000);
        assertEquals(1000, layout.getScrimAnimationDuration());
    }

    @Test
    public void testLayoutParamsCollapseMode() {
        CustomCollapsingToolbarLayout.LayoutParams params =
                new CustomCollapsingToolbarLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.setCollapseMode(CustomCollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
        assertEquals(CustomCollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN, params.getCollapseMode());
        params.setParallaxMultiplier(0.8f);
        assertEquals(0.8f, params.getParallaxMultiplier(), 0.01f);
    }
}
