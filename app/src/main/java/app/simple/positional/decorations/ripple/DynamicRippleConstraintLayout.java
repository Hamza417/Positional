package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DynamicRippleConstraintLayout extends ConstraintLayout {

    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            setBackgroundColor(Color.TRANSPARENT);
            setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        }
    }

    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            setBackgroundColor(Color.TRANSPARENT);
            setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        }
    }
}
