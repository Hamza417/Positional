package app.simple.positional.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.location.LocalLocationProvider
import app.simple.positional.location.callbacks.LocationProviderListener


class LocationService : Service(), LocationProviderListener {
    private var locationProvider: LocalLocationProvider? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        locationProvider = LocalLocationProvider()
        locationProvider!!.init(baseContext, this)
        locationProvider!!.delay = 1000
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        locationProvider!!.removeLocationCallbacks()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        if (locationProvider != null) {
            locationProvider!!.removeLocationCallbacks()
        }
        super.onDestroy()
    }

    override fun onLocationChanged(location: Location) {
        Intent().also { intent ->
            intent.action = "location"
            intent.putExtra("location", location)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
        // Instead send parcelable, it will be done in just single line
        // intent.putExtra("latitude", location.getLatitude());
        // intent.putExtra("longitude", location.getLongitude());
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        //println(provider)
        Intent().also { intent ->
            intent.action = "status"
            intent.putExtra("provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    override fun onProviderEnabled(provider: String) {
        ///println(provider)
        Intent().also { intent ->
            intent.action = "enabled"
            intent.putExtra("isEnabled", true)
            intent.putExtra("provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }

    override fun onProviderDisabled(provider: String) {
        //println(provider)
        Intent().also { intent ->
            intent.action = "enabled"
            intent.putExtra("isEnabled", false)
            intent.putExtra("provider", provider)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }
}