package com.afollestad.aesthetic;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.ScrollView;
import java.lang.reflect.Field;

/** @author Aidan Follestad (afollestad) */
@RestrictTo(LIBRARY_GROUP)
final class EdgeGlowUtil {

  private static final String FIELD_EDGE_GLOW_TOP = "mEdgeGlowTop";

  // Private constructor to prevent instantiation
  private EdgeGlowUtil() {
    throw new UnsupportedOperationException("EdgeGlowUtil is a utility class and cannot be instantiated");
  }

  private static Field edgeGlowFieldEdge;
  private static Field edgeGlowFieldGlow;
  private static Field edgeEffectCompatFieldEdgeEffect;
  private static Field scrollViewFieldEdgeGlowTop;
  private static Field scrollViewFieldEdgeGlowBottom;
  private static Field nestedScrollViewFieldEdgeGlowTop;
  private static Field nestedScrollViewFieldEdgeGlowBottom;
  private static Field listViewFieldEdgeGlowTop;
  private static Field listViewFieldEdgeGlowBottom;
  private static Field recyclerViewFieldEdgeGlowTop;
  private static Field recyclerViewFieldEdgeGlowLeft;
  private static Field recyclerViewFieldEdgeGlowRight;
  private static Field recyclerViewFieldEdgeGlowBottom;
  private static Field viewPagerFieldEdgeGlowLeft;
  private static Field viewPagerFieldEdgeGlowRight;

  private static void invalidateEdgeEffectFields() {
    if (edgeGlowFieldEdge != null
        && edgeGlowFieldGlow != null
        && edgeEffectCompatFieldEdgeEffect != null) {
      return;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      Field edge = null;
      Field glow = null;
      for (Field f : EdgeEffect.class.getDeclaredFields()) {
        String fieldName = f.getName();
        if ("mEdge".equals(fieldName)) {
          edge = f;
        } else if ("mGlow".equals(fieldName)) {
          glow = f;
        } else {
          // default case: do nothing
        }
      }
      edgeGlowFieldEdge = edge;
      edgeGlowFieldGlow = glow;
    } else {
      edgeGlowFieldEdge = null;
      edgeGlowFieldGlow = null;
    }

    Field efc = null;
    try {
      efc = EdgeEffectCompat.class.getDeclaredField("mEdgeEffect");
    } catch (NoSuchFieldException e) {
      if (BuildConfig.DEBUG) e.printStackTrace();
    }
    edgeEffectCompatFieldEdgeEffect = efc;
  }

  private static void invalidateScrollViewFields() {
    if (scrollViewFieldEdgeGlowTop != null && scrollViewFieldEdgeGlowBottom != null) {
      return;
    }
    final Class<?> cls = ScrollView.class;
      if (FIELD_EDGE_GLOW_TOP.equals(fieldName)) {
        f.setAccessible(true);
        scrollViewFieldEdgeGlowTop = f;
      } else if ("mEdgeGlowBottom".equals(fieldName)) {
        scrollViewFieldEdgeGlowTop = f;
      } else if ("mEdgeGlowBottom".equals(fieldName)) {
        f.setAccessible(true);
        scrollViewFieldEdgeGlowBottom = f;
      } else {
        // default case: do nothing
      }
    }
  }

  private static void invalidateNestedScrollViewFields() {
    if (nestedScrollViewFieldEdgeGlowTop != null
        && nestedScrollViewFieldEdgeGlowBottom != null) {
      nestedScrollViewFieldEdgeGlowTop.setAccessible(true);
      nestedScrollViewFieldEdgeGlowBottom.setAccessible(true);
      return;
    }
    Class cls = NestedScrollView.class;
      if (FIELD_EDGE_GLOW_TOP.equals(fieldName)) {
        f.setAccessible(true);
        nestedScrollViewFieldEdgeGlowTop = f;
      } else if ("mEdgeGlowBottom".equals(fieldName)) {
        nestedScrollViewFieldEdgeGlowTop = f;
      } else if ("mEdgeGlowBottom".equals(fieldName)) {
        f.setAccessible(true);
        nestedScrollViewFieldEdgeGlowBottom = f;
      } else {
        // default case: do nothing
      }
    }
  }

  private static void invalidateListViewFields() {
    if (listViewFieldEdgeGlowTop != null && listViewFieldEdgeGlowBottom != null) {
      listViewFieldEdgeGlowTop.setAccessible(true);
      listViewFieldEdgeGlowBottom.setAccessible(true);
      return;
    }
      switch (f.getName()) {
        case FIELD_EDGE_GLOW_TOP:
          f.setAccessible(true);
          listViewFieldEdgeGlowTop = f;
          break;
        case "mEdgeGlowBottom":
          f.setAccessible(true);
          listViewFieldEdgeGlowBottom = f;
          break;
      }
          break;
      }
    }
  }

  private static void invalidateRecyclerViewFields() {
    if (recyclerViewFieldEdgeGlowTop != null
        && recyclerViewFieldEdgeGlowLeft != null
        && recyclerViewFieldEdgeGlowRight != null
        && recyclerViewFieldEdgeGlowBottom != null) {
      recyclerViewFieldEdgeGlowTop.setAccessible(true);
      recyclerViewFieldEdgeGlowLeft.setAccessible(true);
      recyclerViewFieldEdgeGlowRight.setAccessible(true);
      recyclerViewFieldEdgeGlowBottom.setAccessible(true);
      return;
    }
    Class cls = RecyclerView.class;
    for (Field f : cls.getDeclaredFields()) {
      switch (f.getName()) {
        case "mTopGlow":
          f.setAccessible(true);
          recyclerViewFieldEdgeGlowTop = f;
          break;
        case "mBottomGlow":
          f.setAccessible(true);
          recyclerViewFieldEdgeGlowBottom = f;
          break;
        case "mLeftGlow":
          f.setAccessible(true);
          recyclerViewFieldEdgeGlowLeft = f;
          break;
        case "mRightGlow":
          f.setAccessible(true);
          recyclerViewFieldEdgeGlowRight = f;
          break;
      }
    }
  }

  private static void invalidateViewPagerFields() {
    if (viewPagerFieldEdgeGlowLeft != null && viewPagerFieldEdgeGlowRight != null) {
      viewPagerFieldEdgeGlowLeft.setAccessible(true);
      viewPagerFieldEdgeGlowRight.setAccessible(true);
      return;
    }
    Class cls = ViewPager.class;
    for (Field f : cls.getDeclaredFields()) {
      switch (f.getName()) {
        case "mLeftEdge":
          f.setAccessible(true);
          viewPagerFieldEdgeGlowLeft = f;
          break;
        case "mRightEdge":
          f.setAccessible(true);
          viewPagerFieldEdgeGlowRight = f;
          break;
      }
    }
  }

  // Setter methods

  static void setEdgeGlowColor(@NonNull ScrollView scrollView, @ColorInt int color) {
    invalidateScrollViewFields();
    try {
      Object ee;
      ee = scrollViewFieldEdgeGlowTop.get(scrollView);
      setEffectColor(ee, color);
      ee = scrollViewFieldEdgeGlowBottom.get(scrollView);
      setEffectColor(ee, color);
    } catch (Exception ex) {
      if (BuildConfig.DEBUG) ex.printStackTrace();
    }
  }

  static void setEdgeGlowColor(@NonNull NestedScrollView scrollView, @ColorInt int color) {
    invalidateNestedScrollViewFields();
    try {
      Object ee = nestedScrollViewFieldEdgeGlowTop.get(scrollView);
      setEffectColor(ee, color);
      ee = nestedScrollViewFieldEdgeGlowBottom.get(scrollView);
      setEffectColor(ee, color);
    } catch (Exception ex) {
      if (BuildConfig.DEBUG) ex.printStackTrace();
    }
  }

  static void setEdgeGlowColor(@NonNull AbsListView listView, @ColorInt int color) {
    invalidateListViewFields();
    try {
      Object ee = listViewFieldEdgeGlowTop.get(listView);
      setEffectColor(ee, color);
      ee = listViewFieldEdgeGlowBottom.get(listView);
      setEffectColor(ee, color);
    } catch (Exception ex) {
      if (BuildConfig.DEBUG) ex.printStackTrace();
    }
  }

  static void setEdgeGlowColor(
      @NonNull RecyclerView scrollView,
      final @ColorInt int color,
      @Nullable RecyclerView.OnScrollListener scrollListener) {
    invalidateRecyclerViewFields();
    invalidateRecyclerViewFields();
    if (scrollListener == null) {
      scrollListener =
          new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
              super.onScrollStateChanged(recyclerView, newState);
              EdgeGlowUtil.setEdgeGlowColor(recyclerView, color, this);
            }
          };
      scrollView.addOnScrollListener(scrollListener);
    }
    try {
      Object ee = recyclerViewFieldEdgeGlowTop.get(scrollView);
      setEffectColor(ee, color);
      ee = recyclerViewFieldEdgeGlowBottom.get(scrollView);
      setEffectColor(ee, color);
      ee = recyclerViewFieldEdgeGlowLeft.get(scrollView);
      setEffectColor(ee, color);
      ee = recyclerViewFieldEdgeGlowRight.get(scrollView);
      setEffectColor(ee, color);
    } catch (Exception ex) {
      if (BuildConfig.DEBUG) ex.printStackTrace();
    }
  }

  static void setEdgeGlowColor(@NonNull ViewPager pager, @ColorInt int color) {
    invalidateViewPagerFields();
    try {
      Object ee = viewPagerFieldEdgeGlowLeft.get(pager);
      setEffectColor(ee, color);
      ee = viewPagerFieldEdgeGlowRight.get(pager);
      setEffectColor(ee, color);
    } catch (Exception ex) {
      if (BuildConfig.DEBUG) ex.printStackTrace();
    }
  }

  // Utilities

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static void setEffectColor(Object edgeEffect, @ColorInt int color) {
    invalidateEdgeEffectFields();
    if (edgeEffect instanceof EdgeEffectCompat) {
      // EdgeEffectCompat
      try {
        edgeEffectCompatFieldEdgeEffect.setAccessible(true);
        edgeEffect = edgeEffectCompatFieldEdgeEffect.get(edgeEffect);
      } catch (IllegalAccessException e) {
        if (BuildConfig.DEBUG) e.printStackTrace();
        return;
      }
    }
    if (edgeEffect == null) {
      return;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      // EdgeGlow
      try {
        edgeGlowFieldEdge.setAccessible(true);
        final Drawable mEdge = (Drawable) edgeGlowFieldEdge.get(edgeEffect);
        edgeGlowFieldGlow.setAccessible(true);
        final Drawable mGlow = (Drawable) edgeGlowFieldGlow.get(edgeEffect);
        mEdge.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mGlow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mEdge.setCallback(null); // free up any references
        mGlow.setCallback(null); // free up any references
      } catch (Exception ex) {
        if (BuildConfig.DEBUG) ex.printStackTrace();
      }
    } else {
      // EdgeEffect
      ((EdgeEffect) edgeEffect).setColor(color);
    }
  }
}
