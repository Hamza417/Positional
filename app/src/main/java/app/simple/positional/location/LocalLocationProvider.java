package app.simple.positional.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import app.simple.positional.location.callbacks.LocationProviderListener;

/**
 * This class is created for the easy implementation of location provider class among different fragments
 */
public class LocalLocationProvider implements LocationListener {
    
    private LocationProviderListener locationProvider;
    private LocationManager locationManager;
    private Activity activity;
    private Handler handler = new Handler();
    private int delay = 2000; // Default
    private Runnable locationUpdater = new Runnable() {
        @Override
        public void run() {
            fireLocationSearch();
            handler.postDelayed(this, 5000);
        }
    };
    
    public void init(Activity activity, LocationProviderListener locationProvider) {
        this.activity = activity;
        this.locationProvider = locationProvider;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                
                if (location != null) {
                    onLocationChanged(location);
                }
            }
        }
    }
    
    // Spare me, I find this function quite satisfying :P
    public void fireLocationSearch() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, delay, 1f, this);
        }
        else {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            fireLocationSearch();
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        locationProvider.onLocationChanged(location);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        locationProvider.onStatusChanged(provider, status, extras);
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        locationProvider.onProviderEnabled(provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        locationProvider.onProviderDisabled(provider);
    }
    
    public void removeLocationCallbacks() {
        handler.removeCallbacks(locationUpdater);
    }
    
    public void initLocationCallbacks() {
        handler.post(locationUpdater);
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
}
