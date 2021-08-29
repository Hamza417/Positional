package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.text.Spanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.DMSConverter.latitudeAsDD
import app.simple.positional.util.DMSConverter.latitudeAsDM
import app.simple.positional.util.DMSConverter.latitudeAsDMS
import app.simple.positional.util.DMSConverter.longitudeAsDD
import app.simple.positional.util.DMSConverter.longitudeAsDM
import app.simple.positional.util.DMSConverter.longitudeAsDMS
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.UTMConverter
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * This viewmodel receives the location updated by location services
 * and updates the UI in the observing views. The only reason this
 * viewmodel is to prevent loss of location data between switching
 * fragments.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private var filter: IntentFilter = IntentFilter()
    private var locationBroadcastReceiver: BroadcastReceiver

    val location = MutableLiveData<Location>()
    val provider = MutableLiveData<String>()

    val dms = MutableLiveData<Pair<Spanned, Spanned>>()
    val dm = MutableLiveData<Pair<Spanned, Spanned>>()
    val dd = MutableLiveData<Pair<Spanned, Spanned>>()
    val mgrs = MutableLiveData<String>()
    val utm = MutableLiveData<UTMConverter.UTM>()

    init {
        filter.addAction("location")
        filter.addAction("provider")

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            with(intent.getParcelableExtra<Location>("location")!!) {
                                location.postValue(this)
                                dms(this)
                                dm(this)
                                dd(this)
                                mgrs(this)
                                utm(this)
                            }
                        }
                        "provider" -> {
                            provider.postValue(intent.getStringExtra("location_provider")
                                    ?.uppercase(Locale.getDefault()))
                        }
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(locationBroadcastReceiver, filter)
    }

    private fun dms(location: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            with(getApplication<Application>()) {
                dms.postValue(Pair(
                        fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDMS(location.latitude, this)}"),
                        fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDMS(location.longitude, this)}")
                ))
            }
        }
    }

    private fun dm(location: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            with(getApplication<Application>()) {
                dm.postValue(Pair(
                        fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDM(location.latitude, this)}"),
                        fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDM(location.longitude, this)}")
                ))
            }
        }
    }

    private fun dd(location: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            with(getApplication<Application>()) {
                dd.postValue(Pair(
                        fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDD(location.latitude)}"),
                        fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDD(location.longitude)}")
                ))
            }
        }
    }

    private fun mgrs(location: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            with(location) {
                mgrs.postValue(
                        MGRSCoord.fromLatLon(
                                Angle.fromDegreesLatitude(latitude),
                                Angle.fromDegreesLongitude(longitude)
                        ).toString())
            }
        }
    }

    private fun utm(location: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            with(location) {
                utm.postValue(UTMConverter.getUTM(latitude, longitude))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(locationBroadcastReceiver)
    }
}