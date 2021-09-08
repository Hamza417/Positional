package app.simple.positional.decorations.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import app.simple.positional.R;
import app.simple.positional.decorations.ripple.DynamicRippleImageButton;
import app.simple.positional.util.LocationExtension;

public class LocationButton extends DynamicRippleImageButton {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isBlinked = false;
    private final int blinkingDelay = 500;

    public LocationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        handler.postDelayed(blinking, blinkingDelay);
    }

    public void locationIndicatorUpdate(boolean isFixed) {
        if (isFixed) {
            handler.removeCallbacks(blinking);
            if (LocationExtension.INSTANCE.getLocationStatus(getContext())) {
                setImageResource(R.drawable.ic_gps_fixed);
            } else {
                locationIconStatusUpdate();
            }
        } else {
            locationIconStatusUpdate();
        }
    }

    public void locationIconStatusUpdate() {
        if (LocationExtension.INSTANCE.getLocationStatus(getContext())) {
            isBlinked = false;
            setImageResource(R.drawable.ic_gps_not_fixed);
            handler.removeCallbacks(blinking);
            handler.postDelayed(blinking, blinkingDelay);
        } else {
            handler.removeCallbacks(blinking);
            setImageResource(R.drawable.ic_gps_off);
        }
    }

    private final Runnable blinking = new Runnable() {
        @Override
        public void run() {
            if (isBlinked) {
                setImageResource(R.drawable.ic_gps_not_fixed);
                isBlinked = false;
            } else {
                setImageResource(R.drawable.ic_gps_fixed);
                isBlinked = true;
            }
            handler.postDelayed(blinking, blinkingDelay);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(blinking);
        clearAnimation();
    }
}
