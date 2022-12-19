package app.simple.positional.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link androidx.appcompat.widget.AppCompatTextView} but with animated
 * background
 */
public class DynamicRippleTextView extends androidx.appcompat.widget.AppCompatTextView {
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 1.5F));
        }
    }
    
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);
        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 1.5F));
        }
    }
}
