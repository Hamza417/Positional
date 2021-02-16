package app.simple.positional.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DynamicCornerLinearLayout extends LinearLayout {
    
    public DynamicCornerLinearLayout(Context context) {
        super(context);
        LayoutBackground.setBackground(context, this);
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this);
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this);
    }
}
