package app.simple.positional.decorations.maps;

import android.view.View;

import androidx.annotation.NonNull;

public interface MapsToolsCallbacks {
    default void onAlign(@NonNull View view) {

    }

    default void onLocationClicked(@NonNull View view, boolean longPressed) {

    }

    default void onTargetAdd() {

    }

    default void removeTarget(View view) {

    }

    default void onCompassClicked(@NonNull View view) {

    }

    default void onBearingClicked(@NonNull View view) {

    }

    default void onNorthOnly(@NonNull View view) {

    }
}
