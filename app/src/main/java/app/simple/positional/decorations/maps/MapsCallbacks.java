package app.simple.positional.decorations.maps;

import com.google.android.gms.maps.MapView;

public interface MapsCallbacks {
    default void onMapClicked(MapView view) {

    }

    default void onMapInitialized() {

    }
}
