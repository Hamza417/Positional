package app.simple.positional.decorations.popup;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import app.simple.positional.R;
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout;

public class PopupLinearLayout extends DynamicCornerLinearLayout {

    public PopupLinearLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        int p = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        // setPadding(p, p, p, p);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
        setOrientation(LinearLayout.VERTICAL);
        // animateChildren();
    }

    private void animateChildren() {
        if (!isInEditMode()) {
            setScaleY(0);

            animate()
                    .scaleY(1)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator(1.5F))
                    .start();

            post(() -> {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setAlpha(0);
                    getChildAt(i).setTranslationY(-8);

                    getChildAt(i).animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(200)
                            .setStartDelay(200 + (i * 35L))
                            .start();
                }
            });
        }
    }
}
