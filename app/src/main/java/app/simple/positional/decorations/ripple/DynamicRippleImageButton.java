package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

/**
 * Custom dynamic ripple image button
 *
 * @Warning do not use any background
 * with this view. This view will be
 * transparent when created
 */
public class DynamicRippleImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public DynamicRippleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
        }
    }
}
