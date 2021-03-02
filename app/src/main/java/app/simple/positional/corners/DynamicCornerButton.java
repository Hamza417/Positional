package app.simple.positional.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import app.simple.positional.R;
import app.simple.positional.util.ColorAnimator;

public class DynamicCornerButton extends AppCompatButton {
    public DynamicCornerButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    @Override
    public void setClickable(boolean clickable) {
        if (clickable) {
            ColorAnimator.INSTANCE.animateColorChange(this,
                    ContextCompat.getColor(
                            getContext(),
                            R.color.buttonTintClickable));
        }
        else {
            ColorAnimator.INSTANCE.animateColorChange(this,
                    ContextCompat.getColor(
                            getContext(),
                            R.color.buttonTintNotClickable));
        }
        super.setClickable(clickable);
    }
}
