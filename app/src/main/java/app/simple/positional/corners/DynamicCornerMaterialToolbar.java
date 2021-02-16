package app.simple.positional.corners;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DynamicCornerMaterialToolbar extends MaterialToolbar {
    public DynamicCornerMaterialToolbar(@NonNull Context context) {
        super(context);
        LayoutBackground.setBackground(context, this);
    }
    
    public DynamicCornerMaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this);
    }
    
    public DynamicCornerMaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this);
    }
}
