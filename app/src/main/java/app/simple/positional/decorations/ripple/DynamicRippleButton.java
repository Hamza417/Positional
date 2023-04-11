package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class DynamicRippleButton extends androidx.appcompat.widget.AppCompatButton {
    public DynamicRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
        }
    }
}
