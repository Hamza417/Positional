package app.simple.positional.decorations.corners;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import app.simple.positional.R;
import app.simple.positional.decorations.utils.LayoutBackground;

public class DynamicCornerLinearLayout extends LinearLayout {
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            LayoutBackground.setBackground(context, this, attrs);
        }
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            LayoutBackground.setBackground(context, this, attrs);
        }
    }
    
    public DynamicCornerLinearLayout(Context context) {
        super(context);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mainBackground)));
        setOrientation(LinearLayout.VERTICAL);
        if (!isInEditMode()) {
            LayoutBackground.setBackground(context, this, null);
        }

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        setPadding(padding, padding, padding, padding);
    }
}
