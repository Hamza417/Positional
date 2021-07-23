package app.simple.positional.decorations.scrollers;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseSmoothScroller extends RecyclerView.SmoothScroller {

    public static final float DEFAULT_MILLISECONDS_PER_INCH = 25f;

    public static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;

    /**
     * Automatically chooses the best SNAP method depending on the target vector
     */
    public static final int SNAP_AUTOMATIC = -99;

    /**
     * Align child view's left or top with parent view's left or top
     *
     * @see #calculateDtToFit(int, int, int, int, int)
     * @see #calculateDxToMakeVisible(android.view.View, int)
     * @see #calculateDyToMakeVisible(android.view.View, int)
     */
    public static final int SNAP_TO_START = -1;

    /**
     * Align child view's right or bottom with parent view's right or bottom
     *
     * @see #calculateDtToFit(int, int, int, int, int)
     * @see #calculateDxToMakeVisible(android.view.View, int)
     * @see #calculateDyToMakeVisible(android.view.View, int)
     */
    public static final int SNAP_TO_END = 1;

    /**
     * Align child view in the center horizontal or vertical in the parent view
     *
     * @see #calculateDtToFit(int, int, int, int, int)
     * @see #calculateDxToMakeVisible(android.view.View, int)
     * @see #calculateDyToMakeVisible(android.view.View, int)
     */
    public static final int SNAP_TO_CENTER = 2;

    /**
     * <p>Decides if the child should be snapped from start or end, depending on where it
     * currently is in relation to its parent.</p>
     * <p>For instance, if the view is virtually on the left of RecyclerView, using
     * {@code SNAP_TO_ANY} is the same as using {@code SNAP_TO_START}</p>
     *
     * @see #calculateDtToFit(int, int, int, int, int)
     * @see #calculateDxToMakeVisible(android.view.View, int)
     * @see #calculateDyToMakeVisible(android.view.View, int)
     */
    public static final int SNAP_TO_ANY = 0;

    // Trigger a scroll to a further distance than TARGET_SEEK_SCROLL_DISTANCE_PX so that if target
    // view is not laid out until interim target position is reached, we can detect the case before
    // scrolling slows down and reschedule another interim target scroll
    private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2f;

    /**
     * The screen density expressed as dots-per-inch.  May be either
     * {@link DisplayMetrics#DENSITY_LOW}, {@link DisplayMetrics#DENSITY_MEDIUM},
     * or {@link DisplayMetrics#DENSITY_HIGH}.
     */
    protected int mDensityDpi;

    protected Interpolator mSearchingTargetInterpolator;

    protected Interpolator mFoundTargetInterpolator;

    protected PointF mTargetVector;

    /**
     * milliseconds per pixel
     */
    private float msppxSearchingTarget;
    /**
     * milliseconds per inch
     */
    private float mspinSearchingTarget;


    /**
     * milliseconds per pixel
     */
    private float msppxFoundTarget;
    /**
     * milliseconds per inch
     */
    private float mspinFoundTarget;

    private int mHorizontalSnapPreference = SNAP_AUTOMATIC;
    private int mVerticalSnapPreference = SNAP_AUTOMATIC;


    // Temporary variables to keep track of the interim scroll target. These values do not
    // point to a real item position, rather point to an estimated location pixels.
    protected int mInterimTargetDx = 0, mInterimTargetDy = 0;

    public BaseSmoothScroller(Context context) {
        mDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
        mspinFoundTarget = mspinSearchingTarget = DEFAULT_MILLISECONDS_PER_INCH;
        msppxFoundTarget = msppxSearchingTarget = calculateSpeedPerPixel(mspinSearchingTarget);
        mSearchingTargetInterpolator = new LinearInterpolator();
        mFoundTargetInterpolator = new DecelerateInterpolator();
    }

    public float getMillisecondsPerInchSearchingTarget() {
        return mspinSearchingTarget;
    }

    public BaseSmoothScroller setMillisecondsPerInchSearchingTarget(float mspin) {
        this.mspinSearchingTarget = mspin;
        this.msppxSearchingTarget = calculateSpeedPerPixel(mspin);
        return this;
    }

    public float getMillisecondsPerPixelSearchingTarget() {
        return msppxSearchingTarget;
    }

    public BaseSmoothScroller setMillisecondsPerPixelSearchingTarget(float msppx) {
        this.msppxSearchingTarget = msppx;
        return this;
    }

    public float getMillisecondsPerInchFoundTarget() {
        return mspinFoundTarget;
    }

    public BaseSmoothScroller setMillisecondsPerInchFoundTarget(float mspin) {
        this.mspinFoundTarget = mspin;
        this.msppxFoundTarget = calculateSpeedPerPixel(mspin);
        return this;
    }

    public float getMillisecondsPerPixelFoundTarget() {
        return msppxFoundTarget;
    }

    public BaseSmoothScroller setMillisecondsPerPixelFoundTarget(float msppx) {
        this.msppxFoundTarget = msppx;
        return this;
    }

    public int getHorizontalSnapPreference() {
        return mHorizontalSnapPreference;
    }

    public BaseSmoothScroller setHorizontalSnapPreference(int horizontalSnapPreference) {
        this.mHorizontalSnapPreference = horizontalSnapPreference;
        return this;
    }

    public int getVerticalSnapPreference() {
        return mVerticalSnapPreference;
    }

    public BaseSmoothScroller setVerticalSnapPreference(int verticalSnapPreference) {
        this.mVerticalSnapPreference = verticalSnapPreference;
        return this;
    }

    public Interpolator getSearchingTargetInterpolator() {
        return mSearchingTargetInterpolator;
    }

    public BaseSmoothScroller setSearchingTargetInterpolator(Interpolator searchingTargetInterpolator) {
        this.mSearchingTargetInterpolator = searchingTargetInterpolator;
        return this;
    }

    public Interpolator getFoundTargetInterpolator() {
        return mFoundTargetInterpolator;
    }

    public BaseSmoothScroller setFoundTargetInterpolator(Interpolator foundTargetInterpolator) {
        this.mFoundTargetInterpolator = foundTargetInterpolator;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        Log.d(this.getClass().getName(), "onStart");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
        final int dx = calculateDxToMakeVisible(targetView, getFinalHorizontalSnapPreference());
        final int dy = calculateDyToMakeVisible(targetView, getFinalVerticalSnapPreference());
        final int distance = (int) Math.sqrt(dx * dx + dy * dy);
        final int time = calculateTimeForDeceleration(distance);
        if (time > 0) {
            action.update(-dx, -dy, time, mFoundTargetInterpolator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSeekTargetStep(int dx, int dy, RecyclerView.State state, Action action) {
        Log.d(getClass().getName(), "onSeekTargetStep");
        if (getChildCount() == 0) {
            stop();
            return;
        }
        if (mTargetVector != null
                && ((mTargetVector.x * dx < 0 || mTargetVector.y * dy < 0))) {
            Log.e(getClass().getName(), "Scroll happened in the opposite direction"
                    + " of the target. Some calculations are wrong");
        }
        mInterimTargetDx = clampApplyScroll(mInterimTargetDx, dx);
        mInterimTargetDy = clampApplyScroll(mInterimTargetDy, dy);

        if (mInterimTargetDx == 0 && mInterimTargetDy == 0) {
            updateActionForInterimTarget(action);
        } // everything is valid, keep going

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        Log.d(getClass().getName(), "onStop");
        mInterimTargetDx = mInterimTargetDy = 0;
        mTargetVector = null;
    }

    /**
     * Calculates the scroll speed.
     *
     * @return The time (in ms) it should take for each pixel. For instance, if returned value is
     * 2 ms, it means scrolling 1000 pixels with LinearInterpolation should take 2 seconds.
     */
    protected float calculateSpeedPerPixel(float millisecondsPerInch) {
        return millisecondsPerInch / mDensityDpi;
    }

    /**
     * <p>Calculates the time for deceleration so that transition from LinearInterpolator to
     * DecelerateInterpolator looks smooth.</p>
     *
     * @param dx Distance to scroll
     * @return Time for DecelerateInterpolator to smoothly traverse the distance when transitioning
     * from LinearInterpolation
     */
    protected int calculateTimeForDeceleration(int dx) {
        // we want to cover same area with the linear interpolator for the first 10% of the
        // interpolation. After that, deceleration will take control.
        // area under curve (1-(1-x)^2) can be calculated as (1 - x/3) * x * x
        // which gives 0.100028 when x = .3356
        // this is why we divide linear scrolling time with .3356
        return (int) Math.ceil(calculateTimeForScrolling(dx, true) / .3356);
    }

    /**
     * Calculates the time it should take to scroll the given distance (in pixels)
     *
     * @param dx Distance in pixels that we want to scroll
     * @return Time in milliseconds
     * @see #calculateSpeedPerPixel(float)
     */
    protected int calculateTimeForScrolling(int dx, boolean found) {
        float msppx;
        if (found) {
            msppx = msppxFoundTarget;
        } else {
            msppx = msppxSearchingTarget;
        }
        // In a case where dx is very small, rounding may return 0 although dx > 0.
        // To avoid that issue, ceil the result so that if dx > 0, we'll always return positive
        // time.
        return (int) Math.ceil(Math.abs(dx) * msppx);
    }

    /**
     * When scrolling towards a child view, this method defines whether we should align the left
     * or the right edge of the child with the parent RecyclerView.
     *
     * @return SNAP_TO_START, SNAP_TO_END or SNAP_TO_ANY; depending on the current target vector
     * @see #SNAP_TO_START
     * @see #SNAP_TO_END
     * @see #SNAP_TO_ANY
     */
    protected int getActualHorizontalSnapPreference() {
        return mTargetVector == null || mTargetVector.x == 0 ? SNAP_TO_ANY :
                mTargetVector.x > 0 ? SNAP_TO_END : SNAP_TO_START;
    }

    protected int getFinalHorizontalSnapPreference() {
        if (mHorizontalSnapPreference != SNAP_AUTOMATIC) {
            return mHorizontalSnapPreference;
        } else {
            return getActualHorizontalSnapPreference();
        }
    }

    /**
     * When scrolling towards a child view, this method defines whether we should align the top
     * or the bottom edge of the child with the parent RecyclerView.
     *
     * @return SNAP_TO_START, SNAP_TO_END or SNAP_TO_ANY; depending on the current target vector
     * @see #SNAP_TO_START
     * @see #SNAP_TO_END
     * @see #SNAP_TO_ANY
     */
    protected int getActualVerticalSnapPreference() {
        return mTargetVector == null || mTargetVector.y == 0 ? SNAP_TO_ANY :
                mTargetVector.y > 0 ? SNAP_TO_END : SNAP_TO_START;
    }

    protected int getFinalVerticalSnapPreference() {
        if (mVerticalSnapPreference != SNAP_AUTOMATIC) {
            return mVerticalSnapPreference;
        } else {
            return getActualVerticalSnapPreference();
        }
    }

    /**
     * When the target scroll position is not a child of the RecyclerView, this method calculates
     * a direction vector towards that child and triggers a smooth scroll.
     *
     * @see #computeScrollVectorForPosition(int)
     */
    protected void updateActionForInterimTarget(Action action) {
        // find an interim target position
        PointF scrollVector = computeScrollVectorForPosition(getTargetPosition());
        if (scrollVector == null || (scrollVector.x == 0 && scrollVector.y == 0)) {
            Log.e(getClass().getName(), "To support smooth scrolling, you should override \n"
                    + "BaseSmoothScroller#computeScrollVectorForPosition.\n"
                    + "Falling back to instant scroll");
            final int target = getTargetPosition();
            action.jumpTo(target);
            stop();
            return;
        }
        normalize(scrollVector);
        mTargetVector = scrollVector;

        mInterimTargetDx = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.x);
        mInterimTargetDy = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y);
        final int time = calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX, false);
        Log.d(getClass().getName(), "updateActionForInterimTarget : " + time + " : " + mInterimTargetDx + "/" + mInterimTargetDy);
        // To avoid UI hiccups, trigger a smooth scroll to a distance little further than the
        // interim target. Since we track the distance travelled in onSeekTargetStep callback, it
        // won't actually scroll more than what we need.
        action.update((int) (mInterimTargetDx * TARGET_SEEK_EXTRA_SCROLL_RATIO)
                , (int) (mInterimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO)
                , (int) (time * TARGET_SEEK_EXTRA_SCROLL_RATIO), mSearchingTargetInterpolator);
    }

    private int clampApplyScroll(int tmpDt, int dt) {
        final int before = tmpDt;
        tmpDt -= dt;
        if (before * tmpDt <= 0) { // changed sign, reached 0 or was 0, reset
            return 0;
        }
        return tmpDt;
    }

    /**
     * Helper method for {@link #calculateDxToMakeVisible(android.view.View, int)} and
     * {@link #calculateDyToMakeVisible(android.view.View, int)}
     */
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
            snapPreference) {
        switch (snapPreference) {
            case SNAP_TO_CENTER:
                int boxMid = boxStart + (boxEnd - boxStart) / 2;
                int viewMid = viewStart + (viewEnd - viewStart) / 2;
                return boxMid - viewMid;
            case SNAP_TO_START:
                return boxStart - viewStart;
            case SNAP_TO_END:
                return boxEnd - viewEnd;
            case SNAP_TO_ANY:
                final int dtStart = boxStart - viewStart;
                if (dtStart > 0) {
                    return dtStart;
                }
                final int dtEnd = boxEnd - viewEnd;
                if (dtEnd < 0) {
                    return dtEnd;
                }
                break;
            default:
                throw new IllegalArgumentException("snap preference should be one of the"
                        + " constants defined in SmoothScroller, starting with SNAP_");
        }
        return 0;
    }

    /**
     * Calculates the vertical scroll amount necessary to make the given view fully visible
     * inside the RecyclerView.
     *
     * @param view           The view which we want to make fully visible
     * @param snapPreference The edge which the view should snap to when entering the visible
     *                       area. One of {@link #SNAP_TO_START}, {@link #SNAP_TO_END} or
     *                       {@link #SNAP_TO_END}.
     * @return The vertical scroll amount necessary to make the view visible with the given
     * snap preference.
     */
    public int calculateDyToMakeVisible(View view, int snapPreference) {
        final RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (!layoutManager.canScrollVertically()) {
            return 0;
        }
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
        final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
        final int start = layoutManager.getPaddingTop();
        final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
        return calculateDtToFit(top, bottom, start, end, snapPreference);
    }

    /**
     * Calculates the horizontal scroll amount necessary to make the given view fully visible
     * inside the RecyclerView.
     *
     * @param view           The view which we want to make fully visible
     * @param snapPreference The edge which the view should snap to when entering the visible
     *                       area. One of {@link #SNAP_TO_START}, {@link #SNAP_TO_END} or
     *                       {@link #SNAP_TO_END}
     * @return The vertical scroll amount necessary to make the view visible with the given
     * snap preference.
     */
    public int calculateDxToMakeVisible(View view, int snapPreference) {
        final RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (!layoutManager.canScrollHorizontally()) {
            return 0;
        }
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
        final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
        final int start = layoutManager.getPaddingLeft();
        final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
        return calculateDtToFit(left, right, start, end, snapPreference);
    }

    public abstract PointF computeScrollVectorForPosition(int targetPosition);
}
