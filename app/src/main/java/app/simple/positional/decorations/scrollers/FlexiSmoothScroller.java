package app.simple.positional.decorations.scrollers;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

public class FlexiSmoothScroller extends BaseSmoothScroller {

    private Runnable beforeScrollAction;
    private Runnable afterScrollAction;

    public FlexiSmoothScroller(Context context) {
        super(context);
    }

    public Runnable getAfterScrollAction() {
        return afterScrollAction;
    }

    public void setAfterScrollAction(Runnable afterScrollAction) {
        this.afterScrollAction = afterScrollAction;
    }

    public Runnable getBeforeScrollAction() {
        return beforeScrollAction;
    }

    public void setBeforeScrollAction(Runnable beforeScrollAction) {
        this.beforeScrollAction = beforeScrollAction;
    }

    @Override
    protected void onStop() {
        if (afterScrollAction != null) {
            afterScrollAction.run();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        if (beforeScrollAction != null) {
            beforeScrollAction.run();
        }
        super.onStart();
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        Log.d(getClass().getName(), "computeScrollVectorForPosition");
        boolean mShouldReverseLayout = false;
        RecyclerView.LayoutManager lm = getLayoutManager();
        Log.d(getClass().getName(), "computeScrollVectorForPosition lm: " + lm + " : " + getChildCount());
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = lm.getPosition(lm.getChildAt(0));
        final int direction = targetPosition < firstChildPos != mShouldReverseLayout ? -1 : 1;
        Log.d(getClass().getName(), "computeScrollVectorForPosition firstChildPos: " + firstChildPos + ", direction: " + direction);
        if (lm.canScrollHorizontally()) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }
}
