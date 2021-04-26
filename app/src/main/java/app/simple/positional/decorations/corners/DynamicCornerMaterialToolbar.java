package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.positional.decorations.utils.LayoutBackground;
import app.simple.positional.util.StatusBarHeight;

public class DynamicCornerMaterialToolbar extends MaterialToolbar {
    public DynamicCornerMaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
        init(attrs);
    }
    
    public DynamicCornerMaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        LayoutBackground.setBackground(getContext(), this, attrs);
        setPadding(getPaddingLeft(),
                getPaddingTop() + StatusBarHeight.getStatusBarHeight(getContext().getResources()),
                getPaddingRight(),
                getPaddingBottom());
    }
}
