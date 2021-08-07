package app.simple.positional.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class CustomBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    /*
     * we'll use the device's touch slop value to find out when a tap
     * becomes a scroll by checking how far the finger moved to be
     * considered a scroll. if the finger moves more than the touch
     * slop then it's a scroll, otherwise it is just a tap and we
     * ignore the touch events.
     */
    private final int touchSlop;
    private float initialY;
    private boolean ignoreUntilClose;

    public CustomBottomSheetBehavior(
            @NonNull Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(
            @NonNull CoordinatorLayout parent,
            @NonNull V child,
            @NonNull MotionEvent event
    ) {

        /*
         * touch events are ignored if the bottom sheet is already
         * open and we save that state for further processing
         */
        if (getState() == STATE_EXPANDED) {

            ignoreUntilClose = true;
            return super.onInterceptTouchEvent(parent, child, event);

        }

        switch (event.getAction()) {
            /*
             * This is the first event we want to begin observing
             * so we set the initial value for further processing
             * as a positive value to make things easier
             */
            case MotionEvent.ACTION_DOWN:
                initialY = Math.abs(event.getRawY());
                return super.onInterceptTouchEvent(parent, child, event);

            /*
             * if the last bottom sheet state was not open then
             * we check if the current finger movement has exceed
             * the touch slop in which case we return true to tell
             * the system we are consuming the touch event
             * otherwise we let the default handling behavior
             * since we don't care about the direction of the
             * movement we ensure its difference is a positive
             * integer to simplify the condition check
             */
            case MotionEvent.ACTION_MOVE:
                return !ignoreUntilClose
                        && Math.abs(initialY - Math.abs(event.getRawY())) > touchSlop
                        || super.onInterceptTouchEvent(parent, child, event);

            /*
             * Once the tap or movement is completed we reset
             * the initial values to restore normal behavior
             */
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                initialY = 0;
                ignoreUntilClose = false;
                return super.onInterceptTouchEvent(parent, child, event);

            default:
                return super.onInterceptTouchEvent(parent, child, event);
        }
    }
}
