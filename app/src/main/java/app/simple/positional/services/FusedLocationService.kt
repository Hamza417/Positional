package app.simple.positional.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.HandlerThread
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.PermissionUtils
import com.google.android.gms.location.*

/**
 * Service class to use [FusedLocationProviderClient] as location
 * provider and broadcast the location throughout the app.
 */
class FusedLocationService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var handlerThread: HandlerThread

    private val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
    private var broadcastReceiver: BroadcastReceiver? = null

    private var delay: Long = 100

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread = HandlerThread("location_thread")
        intentFilter.addAction(Intent.ACTION_PROVIDER_CHANGED)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, delay)
                .setWaitForAccurateLocation(true)
                .setIntervalMillis(delay)
                .setMinUpdateIntervalMillis(delay)
                .build()

        requestLastLocation()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (result.lastLocation.isNotNull()) {
                    broadcastLocation(result.lastLocation!!)
                }
            }
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Intent().also {
                    it.action = "provider"
                    it.putExtra("location_provider", intent?.action)
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(it)
                }
            }
        }

        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLastLocation()
        if (!handlerThread.isAlive) {
            handlerThread.start()
        }
        return START_REDELIVER_INTENT
    }

    private fun requestLastLocation() {
        if (PermissionUtils.checkPermission(applicationContext)) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful && it.result.isNotNull()) {
                    it.result.provider = "Last Location"
                    broadcastLocation(it.result)
                }

                requestCurrentLocation()
            }
        }
    }

    private fun requestCurrentLocation() {
        if (PermissionUtils.checkPermission(applicationContext)) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, handlerThread.looper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        unregisterReceiver(broadcastReceiver)
        handlerThread.quitSafely()
    }

    private fun broadcastLocation(location: Location) {
        Intent().also { intent ->
            intent.action = "location"
            intent.putExtra("location", location)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }
}
