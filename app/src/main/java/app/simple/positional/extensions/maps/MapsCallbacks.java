package app.simple.positional.extensions.maps;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import app.simple.positional.model.TrailPoint;

public interface MapsCallbacks {
    /**
     * To be called once everything is organized inside the
     * MapView class
     */
    default void onMapInitialized() {

    }

    default void onMapClicked() {

    }

    default void onMapLongClicked(@NotNull LatLng latLng) {

    }

    default void onTouchCoordinates(float x, float y) {

    }

    default void onLineDeleted(@Nullable TrailPoint trailPoint) {

    }

    default void onLineCountChanged(int lineCount) {

    }

    default void onTargetUpdated(LatLng target, LatLng current, float speed) {

    }

    default void onTargetAdd() {

    }

    default void onNavigate() {

    }
}
