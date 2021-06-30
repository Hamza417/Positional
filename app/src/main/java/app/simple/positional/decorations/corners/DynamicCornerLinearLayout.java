package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import app.simple.positional.decorations.utils.LayoutBackground;

public class DynamicCornerLinearLayout extends LinearLayout {
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerLinearLayout(Context context) {
        super(context);
        LayoutBackground.setBackground(context, this, null);
    }
}
