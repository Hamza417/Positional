package app.simple.positional.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

object LocationPrompt {
    private const val TAG = "Location"

    fun checkGooglePlayServices(context: Context): Boolean {
        return when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
            ConnectionResult.SUCCESS -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun displayLocationSettingsRequest(activity: Activity) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2.toLong()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build()).addOnCompleteListener { task ->
            try {
                val response: LocationSettingsResponse = task.getResult(ApiException::class.java)
                Log.i(TAG, "${response.locationSettingsStates}: All location settings are satisfied.")
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                        try {
                            e.status.startResolutionForResult(activity, 2)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
                    }
                }
            }
        }
    }
}
