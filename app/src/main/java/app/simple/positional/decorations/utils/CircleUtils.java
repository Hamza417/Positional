package app.simple.positional.decorations.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.location.Location;
import android.view.animation.DecelerateInterpolator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import app.simple.positional.decorations.interpolators.LatLngInterpolator;

public class CircleUtils {

    static final int duration = 1000;

    /**
     * Method to animate circle to destination location
     *
     * @param location destination location
     * @param circle   circle to be animated
     * @return value animator
     */
    public static ValueAnimator animateCircle(Location location, Circle circle) {
        if (circle != null) {
            LatLng startPosition = circle.getCenter();
            LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());

            float startRadius = (float) circle.getRadius();

            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(duration);
            valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    circle.setCenter(newPosition);
                    circle.setRadius(computeRadius(v, startRadius, location.getAccuracy()));
                } catch (Exception ignored) {
                }
            });

            valueAnimator.start();

            return valueAnimator;
        } else {
            throw new IllegalStateException("Circle object must not be null");
        }
    }

    public static ValueAnimator animateStrokeColor(int endColor, Circle circle) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(circle.getStrokeColor(), endColor);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> circle.setStrokeColor((int) animation.getAnimatedValue()));
        valueAnimator.start();
        return valueAnimator;
    }

    public static ValueAnimator animateFillColor(int endColor, Circle circle) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(circle.getFillColor(), endColor);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> circle.setFillColor((int) animation.getAnimatedValue()));
        valueAnimator.start();
        return valueAnimator;
    }

    private static float computeRadius(float fraction, float start, float end) {
        float normalizeEnd = end - start;
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }
}
