package app.simple.positional.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class DynamicCornersNestedScrollView extends NestedScrollView {
    public DynamicCornersNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornersNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
}
