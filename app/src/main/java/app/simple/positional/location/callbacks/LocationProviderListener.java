package app.simple.positional.location.callbacks;

import android.location.Location;
import android.os.Bundle;

public interface LocationProviderListener {
    void onLocationChanged(Location location);
    
    void onStatusChanged(String provider, int status, Bundle extras);
    
    void onProviderEnabled(String provider);
    
    void onProviderDisabled(String provider);
}
