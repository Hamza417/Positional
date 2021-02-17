package app.simple.positional.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class DynamicCornerCoordinatorLayout extends CoordinatorLayout {
    public DynamicCornerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
}
