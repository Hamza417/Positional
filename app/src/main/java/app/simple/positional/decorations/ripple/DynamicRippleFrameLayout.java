package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DynamicRippleFrameLayout extends FrameLayout {
    public DynamicRippleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            setBackgroundColor(Color.TRANSPARENT);
            setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        }
    }
}
