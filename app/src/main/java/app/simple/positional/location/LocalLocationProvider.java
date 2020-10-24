package app.simple.positional.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;

import androidx.core.app.ActivityCompat;
import app.simple.positional.location.callbacks.LocationProviderListener;

/**
 * This class is created for the easy implementation of location provider class among different fragments
 */
public class LocalLocationProvider implements LocationListener {
    
    private LocationManager locationManager;
    WeakReference <LocationProviderListener> locationProviderWeakReference;
    private Handler handler = new Handler();
    private int delay = 2000; // Default
    private Context context;
    private Runnable locationUpdater = new Runnable() {
        @Override
        public void run() {
            fireLocationSearch();
            handler.postDelayed(this, delay);
        }
    };
    
    public void init(Context context, LocationProviderListener locationProvider) {
        this.context = context;
        locationProviderWeakReference = new WeakReference <>(locationProvider);
        
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    onLocationChanged(location);
                    handler.post(locationUpdater);
                }
                else {
                    fireLocationSearch();
                }
            }
        }
    }
    
    // Spare me, I find this function quite satisfying :P
    public void fireLocationSearch() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, delay, 0f, this);
            }
            else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, delay, 0, this);
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, delay, 0, this);
            }
        }
        else {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            fireLocationSearch();
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        if (locationProviderWeakReference.get() == null) {
            return;
        }
        locationProviderWeakReference.get().onLocationChanged(location);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (locationProviderWeakReference.get() == null) {
            return;
        }
        locationProviderWeakReference.get().onStatusChanged(provider, status, extras);
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        if (locationProviderWeakReference.get() == null) {
            return;
        }
        locationProviderWeakReference.get().onProviderEnabled(provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        if (locationProviderWeakReference.get() == null) {
            return;
        }
        locationProviderWeakReference.get().onProviderDisabled(provider);
    }
    
    public void removeLocationCallbacks() {
        this.context = null;
        locationProviderWeakReference.clear();
        handler.removeCallbacks(locationUpdater);
    }
    
    public void initLocationCallbacks(LocationProviderListener locationProviderListener) {
        locationProviderWeakReference = new WeakReference <>(locationProviderListener);
        handler.post(locationUpdater);
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
}
