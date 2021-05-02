package app.simple.positional.decorations.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import app.simple.positional.R;
import app.simple.positional.decorations.utils.LayoutBackground;
import app.simple.positional.util.ColorUtils;

import static app.simple.positional.decorations.utils.Utils.animateBackground;

public class AnimatedBackgroundTextView extends AppCompatTextView {
    public AnimatedBackgroundTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            animateBackground(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent), this);
        }
        else {
            animateBackground(Color.TRANSPARENT, this);
        }
    
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
    }
}
