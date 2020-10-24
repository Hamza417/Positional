package app.simple.positional.behaviors;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

@SuppressWarnings ("deprecation")
public class MyAppBarLayoutBehavior extends AppBarLayout.Behavior {
    private static final String TAG = "overScroll";
    private static final float TARGET_HEIGHT = 500;
    private View mTargetView;
    private int mParentHeight;
    private int mTargetViewHeight;
    private float mTotalDy;
    private float mLastScale;
    private int mLastBottom;
    private boolean isAnimate;
    
    public MyAppBarLayoutBehavior() {
    }
    
    public MyAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onLayoutChild(@NotNull CoordinatorLayout parent, @NotNull AppBarLayout abl, int layoutDirection) {
        boolean handled = super.onLayoutChild(parent, abl, layoutDirection);
        if (mTargetView == null) {
            mTargetView = parent.findViewWithTag(TAG);
            if (mTargetView != null) {
                initial(abl);
            }
        }
        return handled;
    }
    
    @Override
    public boolean onStartNestedScroll(@NotNull CoordinatorLayout parent, @NotNull AppBarLayout child, @NotNull View directTargetChild, @NotNull View target, int nestedScrollAxes) {
        isAnimate = true;
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes);
    }
    
    @Override
    public void onNestedPreScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull AppBarLayout child, @NotNull View target, int dx, int dy, @NotNull int[] consumed) {
        if (mTargetView != null && ((dy < 0 && child.getBottom() >= mParentHeight) || (dy > 0 && child.getBottom() > mParentHeight))) {
            scale(child, target, dy);
        }
        else {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        }
    }
    
    @Override
    public boolean onNestedPreFling(@NotNull CoordinatorLayout coordinatorLayout, @NotNull AppBarLayout child, @NotNull View target, float velocityX, float velocityY) {
        if (velocityY > 100) {
            isAnimate = false;
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }
    
    @Override
    public void onStopNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull AppBarLayout abl, @NotNull View target) {
        recovery(abl);
        super.onStopNestedScroll(coordinatorLayout, abl, target);
    }
    
    private void initial(AppBarLayout abl) {
        abl.setClipChildren(false);
        mParentHeight = abl.getBottom();
        mTargetViewHeight = mTargetView.getHeight();
    }
    
    private void scale(AppBarLayout abl, View target, int dy) {
        mTotalDy += -dy;
        mTotalDy = Math.min(mTotalDy, TARGET_HEIGHT);
        mLastScale = Math.max(1f, 1f + mTotalDy / TARGET_HEIGHT);
        ViewCompat.setScaleX(mTargetView, mLastScale);
        ViewCompat.setScaleY(mTargetView, mLastScale);
        mLastBottom = mParentHeight + (int) (mTargetViewHeight / 2 * (mLastScale - 1));
        abl.setBottom(mLastBottom);
    }
    
    private void recovery(final AppBarLayout abl) {
        if (mTotalDy > 0) {
            mTotalDy = 0;
            if (isAnimate) {
                ValueAnimator anim = ValueAnimator.ofFloat(mLastScale, 1f).setDuration(200);
                anim.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    ViewCompat.setScaleX(mTargetView, value);
                    ViewCompat.setScaleY(mTargetView, value);
                    abl.setBottom((int) (mLastBottom - (mLastBottom - mParentHeight) * animation.getAnimatedFraction()));
                });
                anim.start();
            }
            else {
                ViewCompat.setScaleX(mTargetView, 1f);
                ViewCompat.setScaleY(mTargetView, 1f);
                abl.setBottom(mParentHeight);
            }
        }
    }
}
