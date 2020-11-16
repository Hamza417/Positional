package app.simple.positional.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

const val TAG = "Location"

@Suppress("deprecation")
fun displayLocationSettingsRequest(context: Context, activity: Activity) {
    val googleApiClient = GoogleApiClient.Builder(context).addApi(LocationServices.API).build()

    googleApiClient.connect()

    val locationRequest = LocationRequest.create()
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationRequest.interval = 10000
    locationRequest.fastestInterval = 10000 / 2.toLong()

    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    val pendingResult: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
    pendingResult.setResultCallback { result ->
        val status: Status = result.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> Log.i(TAG, "All location settings are satisfied.")
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(activity, 2)
                } catch (e: IntentSender.SendIntentException) {
                    Log.i(TAG, "PendingIntent unable to execute request.")
                }
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
        }
    }
}