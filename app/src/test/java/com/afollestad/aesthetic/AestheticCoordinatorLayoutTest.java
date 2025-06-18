package com.afollestad.aesthetic;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AestheticCoordinatorLayoutTest {

    private Context mockContext;
    private AttributeSet mockAttrs;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockAttrs = mock(AttributeSet.class);
    }

    @Test
    public void testConstructors() {
        new AestheticCoordinatorLayout(mockContext);
        new AestheticCoordinatorLayout(mockContext, mockAttrs);
        new AestheticCoordinatorLayout(mockContext, mockAttrs, 0);
    }

    @Test
    public void testOnAttachedToWindowAndDetached() {
        AestheticCoordinatorLayout layout = new AestheticCoordinatorLayout(mockContext);
        // Mocks for children
        AppBarLayout appBarLayout = mock(AppBarLayout.class);
        CollapsingToolbarLayout collapsingToolbarLayout = mock(CollapsingToolbarLayout.class);
        AestheticToolbar toolbar = mock(AestheticToolbar.class);
        View colorView = mock(View.class);
        ColorDrawable colorDrawable = new ColorDrawable(Color.RED);

        // Setup child hierarchy
        when(appBarLayout.getChildCount()).thenReturn(1);
        when(appBarLayout.getChildAt(0)).thenReturn(collapsingToolbarLayout);
        when(collapsingToolbarLayout.getChildCount()).thenReturn(2);
        when(collapsingToolbarLayout.getChildAt(0)).thenReturn(toolbar);
        when(collapsingToolbarLayout.getChildAt(1)).thenReturn(colorView);
        when(colorView.getBackground()).thenReturn(colorDrawable);

        // Setup layout children
        when(layout.getChildCount()).thenReturn(1);
        when(layout.getChildAt(0)).thenReturn(appBarLayout);

        // Call lifecycle methods
        layout.onAttachedToWindow();
        layout.onDetachedFromWindow();
    }

    @Test
    public void testTintMenuStatic() {
        AestheticToolbar toolbar = mock(AestheticToolbar.class);
        Menu menu = mock(Menu.class);
        ActiveInactiveColors colors = ActiveInactiveColors.create(Color.BLACK, Color.GRAY);

        // Should not throw
        AestheticCoordinatorLayout.tintMenu(toolbar, menu, colors);
    }

    @Test
    public void testOnOffsetChanged() {
        AestheticCoordinatorLayout layout = new AestheticCoordinatorLayout(mockContext);
        AppBarLayout appBarLayout = mock(AppBarLayout.class);
        layout.onOffsetChanged(appBarLayout, 10);
        // Should not throw
    }
}
