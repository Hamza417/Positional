package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import app.simple.positional.decorations.utils.LayoutBackground;
import app.simple.positional.util.StatusBarHeight;

public class DynamicCornerRecyclerView extends RecyclerView {
    public DynamicCornerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DynamicCornerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        if (StatusBarHeight.isLandscape(getContext())) {
            setPadding(getPaddingLeft(),
                    getPaddingTop() + StatusBarHeight.getStatusBarHeight(getResources()),
                    getPaddingRight(),
                    getPaddingBottom());
        }

        LayoutBackground.setBackground(getContext(), this, attrs);
    }
}
