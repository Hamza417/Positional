package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.core.content.res.ResourcesCompat;

import app.simple.positional.R;
import app.simple.positional.util.ColorUtils;

public class DynamicRippleButton extends androidx.appcompat.widget.AppCompatButton {
    public DynamicRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setAllCaps(false);
        setTypeface(ResourcesCompat.getFont(context, R.font.bold));
        setTextColor(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent));

        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
        }
    }
}
