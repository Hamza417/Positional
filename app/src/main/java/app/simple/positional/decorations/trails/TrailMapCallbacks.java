package app.simple.positional.decorations.trails;

import androidx.annotation.Nullable;

import app.simple.positional.model.TrailData;

public interface TrailMapCallbacks {
    default void onMapClicked() {

    }

    default void onMapInitialized() {

    }

    default void onLineDeleted(@Nullable TrailData trailData) {

    }

    default void onNoLinesLeft() {

    }

    default void onLineCountChanged(int lineCount) {

    }
}
