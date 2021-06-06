package app.simple.positional.decorations.popup;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import app.simple.positional.R;
import app.simple.positional.decorations.corners.DynamicCornerFrameLayout;

public class PopupFrameLayout extends DynamicCornerFrameLayout {
    public PopupFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    private void init() {
        int p = getResources().getDimensionPixelOffset(R.dimen.options_container_horizontal_padding);
        setPadding(p, p, p, p);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
    }
}
