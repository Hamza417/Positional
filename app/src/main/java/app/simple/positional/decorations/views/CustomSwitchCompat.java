package app.simple.positional.decorations.views;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import app.simple.positional.R;

public class CustomSwitchCompat extends SwitchCompat {
    
    private final int animationDuration = getResources().getInteger(R.integer.animation_duration);
    
    public CustomSwitchCompat(@NonNull Context context) {
        super(context);
    }
    
    public CustomSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CustomSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked()) {
                ((TransitionDrawable) getTrackDrawable()).startTransition(animationDuration);
            }
            else {
                ((TransitionDrawable) getTrackDrawable()).reverseTransition(animationDuration);
            }
            
            Log.d("Switch", String.valueOf(getTrackDrawable().getState().length));
            listener.onCheckedChanged(buttonView, isChecked);
        });
    }
}
