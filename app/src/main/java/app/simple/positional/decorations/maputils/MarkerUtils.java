package app.simple.positional.decorations.maputils;

import android.animation.ValueAnimator;
import android.location.Location;
import android.view.animation.DecelerateInterpolator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import app.simple.positional.decorations.interpolators.LatLngInterpolator;
import app.simple.positional.preferences.TrailPreferences;

public class MarkerUtils {
    /**
     * Method to animate marker to destination location
     *
     * @param location destination location (must contain bearing attribute, to ensure
     *                 marker rotation will work correctly)
     * @param marker   marker to be animated
     */
    public static void animateMarker(Location location, Marker marker) {
        if (marker != null) {
            LatLng startPosition = marker.getPosition();
            LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());

            float startRotation = marker.getRotation();
            float rotation;

            if (location.getSpeed() > 0) {
                rotation = location.getBearing();
            } else {
                rotation = 0F;
            }

            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            valueAnimator.addUpdateListener(animation -> {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);
                    marker.setRotation(computeRotation(v, startRotation, rotation));
                } catch (Exception ignored) {
                }
            });

            valueAnimator.start();
        }
    }

    /**
     * Method to animate marker to destination location
     *
     * @param location destination location (must contain bearing attribute, to ensure
     *                 marker rotation will work correctly)
     * @param marker   marker to be animated
     */
    public static void animateMarker(Location location, Marker marker, float rotation) {
        if (marker != null) {
            LatLng startPosition = marker.getPosition();
            LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());

            float startRotation = marker.getRotation();

            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);
                    marker.setRotation(computeRotation(v, startRotation, rotation));
                } catch (Exception ignored) {
                }
            });

            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
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
