package app.simple.positional.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.text.Spanned
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.DistanceSingleton
import app.simple.positional.util.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LocationService : Service(), LocationListener {
    private val distanceSingleton = DistanceSingleton
    private var locationManager: LocationManager? = null
    private var handler = Handler(Looper.getMainLooper())
    private var delay: Long = 1000
    private val requestCode = 2
    private val notificationID = 2486
    private val actionClose = "ACTION_CLOSE"
    private var isDestroying = false
    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = baseContext.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        requestLastKnownLocation()
        handleIntent(intent)
        notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        return START_REDELIVER_INTENT
    }

    private fun handleIntent(intent_: Intent) {
        if (intent_.action == actionClose) {
            Intent().also { intent ->
                intent.action = "finish"
                LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        if (locationManager != null) {
            locationManager?.removeUpdates(this)
        }
        handler.removeCallbacks(locationUpdater)
        stopSelf()
    }

    override fun onDestroy() {
        removeCallbacks()
        isDestroying = true
        notificationManager?.cancel(notificationID)
        super.onDestroy()
    }

    override fun onLocationChanged(location: Location) {
        Intent().also { intent ->
            intent.action = "location"
            intent.putExtra("location", location)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }

        measureDistance(location)
        // Instead send parcelable, it will be done in just single line
        // intent.putExtra("latitude", location.getLatitude());
        // intent.putExtra("longitude", location.getLongitude());
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
        requestLastKnownLocation()
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
            fireLocationSearch()
            handler.postDelayed(this, delay)
        }
    }

    fun fireLocationSearch() {
        requestLocation()
    }

    private fun requestLastKnownLocation() {
        if (checkPermission()) return

        var location: Location? = null

        when {
            locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            locationManager!!.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> {
                location = locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
        }

        if (location != null) {
            onLocationChanged(location)
            handler.post(locationUpdater)
        } else {
            fireLocationSearch()
        }
    }

    private fun requestLocation() {
        if (checkPermission()) return

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

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            !(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        } else {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                false
            } else !(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun removeCallbacks() {
        locationManager?.removeUpdates(this)
        handler.removeCallbacks(locationUpdater)
    }

    private fun measureDistance(location: Location) {
        if (location.speed == 0f) {
            if (!distanceSingleton.isMapPanelVisible!!) {
                if (distanceSingleton.isInitialLocationSet!!) {
                    val result = FloatArray(1)
                    Location.distanceBetween(distanceSingleton.distanceCoordinates!!.latitude, distanceSingleton.distanceCoordinates!!.longitude, location.latitude, location.longitude, result)
                    distanceSingleton.totalDistance = distanceSingleton.totalDistance?.plus(result[0])
                    distanceSingleton.distanceCoordinates = LatLng(location.latitude, location.longitude)
                } else {
                    distanceSingleton.distanceCoordinates = LatLng(location.latitude, location.longitude)
                    distanceSingleton.initialPointCoordinates = LatLng(location.latitude, location.longitude)
                    distanceSingleton.isInitialLocationSet = true
                }
            }

            CoroutineScope(Dispatchers.Default).launch {
                if (!GPSPreferences().isNotificationOn(this@LocationService)) {
                    notificationManager?.cancel(notificationID)
                    return@launch
                }

                val isMetric = MainPreferences().getUnit(context = baseContext)

                val displacementValue = FloatArray(1)
                val distanceValue = FloatArray(1)
                Location.distanceBetween(
                        distanceSingleton.initialPointCoordinates!!.latitude,
                        distanceSingleton.initialPointCoordinates!!.longitude,
                        location.latitude,
                        location.longitude,
                        displacementValue
                )

                Location.distanceBetween(
                        distanceSingleton.initialPointCoordinates!!.latitude,
                        distanceSingleton.initialPointCoordinates!!.longitude,
                        location.latitude,
                        location.longitude,
                        distanceValue
                )

                val displacement = if (displacementValue[0] < 1000) {
                    if (isMetric) {
                        "<b>Displacement:</b> ${round(displacementValue[0].toDouble(), 2)} m<br>"
                    } else {
                        "<b>Displacement:</b> ${round(displacementValue[0].toDouble().toFeet(), 2)} ft<br>"
                    }
                } else {
                    if (isMetric) {
                        "<b>Displacement:</b> ${round(displacementValue[0].toKilometers().toDouble(), 2)} km<br>"
                    } else {
                        "<b>Displacement:</b> ${round(displacementValue[0].toMiles().toDouble().toFeet(), 2)} miles<br>"
                    }
                }

                val distance = if (displacementValue[0] < 1000) {
                    if (isMetric) {
                        "<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toDouble(), 2)} m<br>"
                    } else {
                        "<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toDouble().toFeet(), 2)} ft<br>"
                    }
                } else {
                    if (isMetric) {
                        "<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toKilometers().toDouble(), 2)} km<br>"
                    } else {
                        "<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toMiles().toDouble().toFeet(), 2)} miles<br>"
                    }
                }
                val direction: String
                val speed = if (isMetric) "<b>Speed:</b> ${round(location.speed.toDouble().toKiloMetersPerHour(), 2)} km/h<br>" else "<b>Speed:</b> ${round(location.speed.toDouble().toMilesPerHour(), 2)} mph/h<br>"

                if (location.speed > 0f) {
                    distanceSingleton.totalDistance = distanceSingleton.totalDistance?.plus(distanceValue[0])
                    val dir = getDirection(
                            distanceSingleton.distanceCoordinates!!.latitude,
                            distanceSingleton.distanceCoordinates!!.longitude,
                            location.latitude,
                            location.longitude
                    )
                    distanceSingleton.distanceCoordinates = LatLng(location.latitude, location.longitude)
                    direction = "<b>Direction:</b> ${round(dir, 2)}° ${getDirectionCodeFromAzimuth(dir)}"
                } else {
                    direction = "<b>Direction:</b> N/A"
                }

                val finalSpanned = fromHtml("$speed $distance $displacement $direction")

                withContext(Dispatchers.Main) {
                    if (!isDestroying) {
                        showNotification(finalSpanned)
                    }
                }
            }
        }
    }

    private fun showNotification(messageBody: Spanned?) {
        val activityIntent = Intent(this, LocationService::class.java)
        activityIntent.action = actionClose
        val contentIntent = PendingIntent.getService(this, requestCode, activityIntent, PendingIntent.FLAG_ONE_SHOT)
        //val broadcastIntent = Intent(this, NotificationReceiver::class.java)

        val channelId = "notification_channel_id_for_positional"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_place)
                .setColor(Color.parseColor("#1B9CFF"))
                .setSubText("Movement Updates")
                //.setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(0, "Close App", contentIntent)
                .setOngoing(true)
                .setContentIntent(contentIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Movement Update",
                    NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(channel)
        }

        notificationManager?.notify(notificationID, notificationBuilder.build())
    }
}