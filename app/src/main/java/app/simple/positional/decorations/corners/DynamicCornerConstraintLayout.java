package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import app.simple.positional.decorations.utils.LayoutBackground;

public class DynamicCornerConstraintLayout extends ConstraintLayout {

    public DynamicCornerConstraintLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (!isInEditMode()) {
            LayoutBackground.setBackground(context, this, attrs);
        }
    }
}
