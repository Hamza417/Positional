package app.simple.positional.decorations.maps;

import org.osmdroid.views.MapView;

public interface OsmMapsCallbacks {
    default void onMapClicked(MapView view) {
    
    }
}
