package app.simple.positional.services

import android.app.Service
import android.content.Intent
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.PermissionUtils

class LocationService : Service(), LocationListener {

    private var locationManager: LocationManager? = null
    private var handler = Handler(Looper.getMainLooper())
    private var delay: Long = 100

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = baseContext.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        postLocationRunnable()
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        locationManager?.removeUpdates(this)
        handler.removeCallbacks(locationUpdater)
        super.onDestroy()
    }

    override fun onLocationChanged(location: Location) {
        Intent().also { intent ->
            intent.action = "location"
            intent.putExtra("location", location)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    // only for API < 29
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Intent().also { intent ->
            intent.action = "provider"
            intent.putExtra("location_provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    override fun onProviderEnabled(provider: String) {
        postLocationRunnable()
        Intent().also { intent ->
            intent.action = "provider"
            intent.putExtra("location_provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    override fun onProviderDisabled(provider: String) {
        handler.removeCallbacks(locationUpdater)
        Intent().also { intent ->
            intent.action = "provider"
            intent.putExtra("location_provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    private val locationUpdater: Runnable = object : Runnable {
        override fun run() {
            requestLocation()
            handler.postDelayed(this, delay)
        }
    }

    private fun requestLocation() {
        if (PermissionUtils.checkPermission(applicationContext)) {
            when {
                locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, delay, 0f, this)
                }
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, delay, 0f, this)
                }
                else -> {
                    locationManager?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, delay, 0f, this)
                }
            }
        }
    }

    private fun postLocationRunnable() {
        handler.removeCallbacks(locationUpdater)
        handler.post(locationUpdater)
    }
}