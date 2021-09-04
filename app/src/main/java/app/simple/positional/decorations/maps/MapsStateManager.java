package app.simple.positional.decorations.maps;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapsStateManager {
    
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";
    
    private static final String PREFS_NAME = "mapCameraState";
    private final SharedPreferences mapStatePrefs;
    
    public MapsStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveMapState(GoogleMap googleMap) {
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = googleMap.getCameraPosition();
        
        editor.putFloat(LATITUDE, (float) position.target.latitude);
        editor.putFloat(LONGITUDE, (float) position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(TILT, position.tilt);
        editor.putFloat(BEARING, position.bearing);
        editor.putInt(MAPTYPE, googleMap.getMapType());
        editor.apply();
    }
    
    public CameraPosition getSavedCameraPosition() {
        double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
        if (latitude == 0) {
            return null;
        }
        double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
        LatLng target = new LatLng(latitude, longitude);
        
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
    
        return new CameraPosition(target, zoom, tilt, bearing);
    }
    
    public int getSavedMapType() {
        return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }
}
