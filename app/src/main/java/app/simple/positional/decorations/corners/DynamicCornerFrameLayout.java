package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.simple.positional.decorations.utils.LayoutBackground;

public class DynamicCornerFrameLayout extends FrameLayout {
    public DynamicCornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DynamicCornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public DynamicCornerFrameLayout(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (!isInEditMode()) {
            LayoutBackground.setBackground(getContext(), this, attrs);
        }
    }
}
