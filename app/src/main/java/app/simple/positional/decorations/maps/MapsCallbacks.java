package app.simple.positional.decorations.maps;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

public interface MapsCallbacks {
    default void onMapClicked(MapView view) {

    }

    default void onMapInitialized() {

    }

    default void onMapLongClicked(LatLng latLng) {

    }

    default void onTargetUpdated(LatLng target, LatLng current) {

    }

    default void onTargetAdd() {

    }
}
