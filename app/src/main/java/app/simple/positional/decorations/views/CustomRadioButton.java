package app.simple.positional.decorations.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import app.simple.positional.R;
import app.simple.positional.util.ColorUtils;

import static app.simple.positional.decorations.utils.Utils.animateTint;

public class CustomRadioButton extends androidx.appcompat.widget.AppCompatRadioButton {
    
    public CustomRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CustomRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    public void setChecked(boolean checked) {
        if (checked) {
            animateTint(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent), this);
        }
        else {
            animateTint(ContextCompat.getColor(getContext(), R.color.iconRegular), this);
        }
        
        super.setChecked(checked);
    }
}
