package com.afollestad.aesthetic;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.util.Pair;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import java.lang.reflect.Field;

/** @author Aidan Follestad (afollestad) */
public class AestheticCoordinatorLayout extends CoordinatorLayout
    implements AppBarLayout.OnOffsetChangedListener {

  private Disposable toolbarColorSubscription;
  private Disposable statusBarColorSubscription;
  private AppBarLayout appBarLayout;
  private View colorView;
  private AestheticToolbar toolbar;
  private CollapsingToolbarLayout collapsingToolbarLayout;

  private int toolbarColor;
  private ActiveInactiveColors iconTextColors;
  private int lastOffset = -1;

  public AestheticCoordinatorLayout(Context context) {
    super(context);
  }

  public AestheticCoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AestheticCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @SuppressWarnings("unchecked")
  private static void tintMenu(
      @NonNull AestheticToolbar toolbar, @Nullable Menu menu, final ActiveInactiveColors colors) {
    tintNavigationAndOverflow(toolbar, colors);
    tintCollapseIcon(toolbar, colors);
    tintActionMenuItems(toolbar, colors);

    if (menu == null) {
      menu = toolbar.getMenu();
    }
    ViewUtil.tintToolbarMenu(toolbar, menu, colors);
  }

  private static void tintNavigationAndOverflow(
      @NonNull AestheticToolbar toolbar, final ActiveInactiveColors colors) {
    if (toolbar.getNavigationIcon() != null) {
      toolbar.setNavigationIcon(toolbar.getNavigationIcon(), colors.activeColor());
    }
    Util.setOverflowButtonColor(toolbar, colors.activeColor());
  }

  private static void tintCollapseIcon(
      @NonNull AestheticToolbar toolbar, final ActiveInactiveColors colors) {
    // Reflection removed: Use getNavigationIcon() and setNavigationIcon() if possible
    Drawable collapseIcon = toolbar.getNavigationIcon();
    if (collapseIcon != null) {
      toolbar.setNavigationIcon(TintHelper.createTintedDrawable(collapseIcon, colors.toEnabledSl()));
    }
  }

  private static void tintActionMenuItems(
      @NonNull AestheticToolbar toolbar, final ActiveInactiveColors colors) {
    final PorterDuffColorFilter colorFilter =
        new PorterDuffColorFilter(colors.activeColor(), PorterDuff.Mode.SRC_IN);
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      final View v = toolbar.getChildAt(i);
      // We can't iterate through the toolbar.getMenu() here, because we need the ActionMenuItemView.
      if (v instanceof ActionMenuView) {
        tintActionMenuView((ActionMenuView) v, colorFilter);
      }
    }
  }

  private static void tintActionMenuView(ActionMenuView actionMenuView, PorterDuffColorFilter colorFilter) {
    for (int j = 0; j < actionMenuView.getChildCount(); j++) {
      final View innerView = actionMenuView.getChildAt(j);
      if (innerView instanceof ActionMenuItemView) {
        tintActionMenuItemView((ActionMenuItemView) innerView, colorFilter);
      }
    }
  }

  private static void tintActionMenuItemView(ActionMenuItemView itemView, PorterDuffColorFilter colorFilter) {
    int drawablesCount = itemView.getCompoundDrawables().length;
    for (int k = 0; k < drawablesCount; k++) {
      if (itemView.getCompoundDrawables()[k] != null) {
        itemView.getCompoundDrawables()[k].setColorFilter(colorFilter);
      }
    }
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    findToolbarAndColorView();
    subscribeToolbarColors();
    subscribeStatusBarColors();
  }

  private void findToolbarAndColorView() {
    // Find the toolbar and color view used to blend the scroll transition
    if (getChildCount() > 0 && getChildAt(0) instanceof AppBarLayout) {
      appBarLayout = (AppBarLayout) getChildAt(0);
      if (appBarLayout.getChildCount() > 0
          && appBarLayout.getChildAt(0) instanceof CollapsingToolbarLayout) {
        collapsingToolbarLayout = (CollapsingToolbarLayout) appBarLayout.getChildAt(0);
        findToolbarAndColorViewInCollapsingLayout(collapsingToolbarLayout);
      }
    }
  }

  private void findToolbarAndColorViewInCollapsingLayout(CollapsingToolbarLayout collapsingToolbarLayout) {
    for (int i = 0; i < collapsingToolbarLayout.getChildCount(); i++) {
      if (this.toolbar != null && this.colorView != null) {
        break;
      }
      View child = collapsingToolbarLayout.getChildAt(i);
      if (child instanceof AestheticToolbar) {
        this.toolbar = (AestheticToolbar) child;
      } else if (child.getBackground() instanceof ColorDrawable) {
        this.colorView = child;
      }
    }
  }

  private void subscribeToolbarColors() {
    if (toolbar != null && colorView != null) {
      this.appBarLayout.addOnOffsetChangedListener(this);
      toolbarColorSubscription =
          Observable.combineLatest(
                  toolbar.colorUpdated(),
                  Aesthetic.get(getContext()).colorIconTitle(toolbar.colorUpdated()),
                  new BiFunction<
                      Integer, ActiveInactiveColors, Pair<Integer, ActiveInactiveColors>>() {
                    @Override
                    public Pair<Integer, ActiveInactiveColors> apply(
                        Integer integer, ActiveInactiveColors activeInactiveColors) {
                      return Pair.create(integer, activeInactiveColors);
                    }
                  })
              .compose(Rx.<Pair<Integer, ActiveInactiveColors>>distinctToMainThread())
              .subscribe(
                  new Consumer<Pair<Integer, ActiveInactiveColors>>() {
                    @Override
                    public void accept(@NonNull Pair<Integer, ActiveInactiveColors> result) {
                      toolbarColor = result.first;
                      iconTextColors = result.second;
                      invalidateColors();
                    }
                  },
                  onErrorLogAndRethrow());
    }
  }

  private void subscribeStatusBarColors() {
    if (collapsingToolbarLayout != null) {
      statusBarColorSubscription =
          Aesthetic.get(getContext())
              .colorStatusBar()
              .compose(Rx.<Integer>distinctToMainThread())
              .subscribe(
                  new Consumer<Integer>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Integer color) {
                      collapsingToolbarLayout.setContentScrimColor(color);
                      collapsingToolbarLayout.setStatusBarScrimColor(color);
                    }
                  },
                  onErrorLogAndRethrow());
    }
  }

  @Override
  public void onDetachedFromWindow() {
    if (toolbarColorSubscription != null) {
      toolbarColorSubscription.dispose();
    }
    if (statusBarColorSubscription != null) {
      statusBarColorSubscription.dispose();
    }
    if (this.appBarLayout != null) {
      this.appBarLayout.removeOnOffsetChangedListener(this);
      this.appBarLayout = null;
    }
    this.toolbar = null;
    this.colorView = null;
    super.onDetachedFromWindow();
  }

  @Override
  public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
    if (lastOffset == Math.abs(verticalOffset)) {
      return;
    }
    lastOffset = Math.abs(verticalOffset);
    invalidateColors();
  }

  private void invalidateColors() {
    if (iconTextColors == null) {
      return;
    }

    final int maxOffset = appBarLayout.getMeasuredHeight() - toolbar.getMeasuredHeight();
    final float ratio = (float) lastOffset / (float) maxOffset;

    final int colorViewColor = ((ColorDrawable) colorView.getBackground()).getColor();
    final int blendedColor = Util.blendColors(colorViewColor, toolbarColor, ratio);
    final int collapsedTitleColor = iconTextColors.activeColor();
    final int expandedTitleColor = Util.isColorLight(colorViewColor) ? Color.BLACK : Color.WHITE;
    final int blendedTitleColor = Util.blendColors(expandedTitleColor, collapsedTitleColor, ratio);

    toolbar.setBackgroundColor(blendedColor);

    collapsingToolbarLayout.setCollapsedTitleTextColor(collapsedTitleColor);
    collapsingToolbarLayout.setExpandedTitleColor(expandedTitleColor);

    tintMenu(
        toolbar,
        toolbar.getMenu(),
        ActiveInactiveColors.create(blendedTitleColor, Util.adjustAlpha(blendedColor, 0.7f)));
  }
}
