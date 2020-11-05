package app.simple.positional.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class CustomCoordinatorLayout extends CoordinatorLayout {
    
    private View proxyView;
    
    public CustomCoordinatorLayout(@NonNull Context context) {
        super(context);
    }
    
    public CustomCoordinatorLayout(
            @NonNull Context context,
            @Nullable AttributeSet attrs
                                  ) {
        super(context, attrs);
    }
    
    public CustomCoordinatorLayout(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
                                  ) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    public boolean isPointInChildBounds(
            @NonNull View child,
            int x,
            int y
                                       ) {
        
        if (super.isPointInChildBounds(child, x, y)) {
            return true;
        }
        
        // we want to intercept touch events if they are
        // within the proxy view bounds, for this reason
        // we instruct the coordinator layout to check
        // if this is true and let the touch delegation
        // respond to that result
        if (proxyView != null) {
            return super.isPointInChildBounds(proxyView, x, y);
        }
        
        return false;
        
    }
    
    // for this example we are only interested in intercepting
    // touch events for a single view, if more are needed use
    // a List<View> viewList instead and iterate in
    // isPointInChildBounds
    public void setProxyView(View proxyView) {
        this.proxyView = proxyView;
    }
}
