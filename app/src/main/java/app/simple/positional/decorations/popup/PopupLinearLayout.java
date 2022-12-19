package app.simple.positional.decorations.popup;

import android.content.Context;
import android.content.res.ColorStateList;
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
        setPadding(p, p, p, p);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
        setOrientation(LinearLayout.VERTICAL);
    }
}
