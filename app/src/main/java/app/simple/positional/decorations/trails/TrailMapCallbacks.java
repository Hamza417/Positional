package app.simple.positional.decorations.trails;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import app.simple.positional.model.TrailData;

public interface TrailMapCallbacks {
    default void onMapClicked() {

    }

    default void onMapLongClicked(@NotNull LatLng latLng) {

    }

    default void onTouchCoordinates(float x, float y) {

    }

    default void onMapInitialized() {

    }

    default void onLineDeleted(@Nullable TrailData trailData) {

    }

    default void onLineCountChanged(int lineCount) {

    }
}
