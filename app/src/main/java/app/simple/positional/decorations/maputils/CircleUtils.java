package app.simple.positional.decorations.maputils;

import android.animation.ValueAnimator;
import android.location.Location;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import app.simple.positional.decorations.interpolators.LatLngInterpolator;

public class CircleUtils {
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
            valueAnimator.setDuration(1000);
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
            throw new IllegalStateException();
        }
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
