package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.text.toSpannable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.math.MathExtensions
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ArrayHelper.isLastValueSame
import app.simple.positional.util.DMSConverter.latitudeAsDD
import app.simple.positional.util.DMSConverter.latitudeAsDM
import app.simple.positional.util.DMSConverter.latitudeAsDMS
import app.simple.positional.util.DMSConverter.longitudeAsDD
import app.simple.positional.util.DMSConverter.longitudeAsDM
import app.simple.positional.util.DMSConverter.longitudeAsDMS
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.UTMConverter
import app.simple.positional.util.isNetworkAvailable
import com.google.android.gms.maps.model.LatLng
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * This viewmodel receives the location updated by location services
 * and updates the UI in the observing views. The only reason this
 * viewmodel is to prevent loss of location data between switching
 * fragments.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private var filter: IntentFilter = IntentFilter()
    private var locationBroadcastReceiver: BroadcastReceiver

    private val accuracyData = ArrayList<Float>()
    private val altitudeData = ArrayList<Float>()

    val location = MutableLiveData<Location>()
    val provider = MutableLiveData<String>()

    val dms = MutableLiveData<Pair<Spanned, Spanned>>()
    val dm = MutableLiveData<Pair<Spanned, Spanned>>()
    val dd = MutableLiveData<Pair<Spanned, Spanned>>()
    val mgrs = MutableLiveData<String>()
    val utm = MutableLiveData<UTMConverter.UTM>()
    val latency = MutableLiveData<Spannable>()

    val address : MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            if(MainPreferences.isCustomCoordinate()) {
                with(MainPreferences.getCoordinates()) {
                    address(LatLng(this[0].toDouble(), this[1].toDouble()), true)
                }
            }
        }
    }

    val accuracyGraphData = MutableLiveData<ArrayList<Float>>()
    val altitudeGraphData = MutableLiveData<ArrayList<Float>>()

    private var lastLatencyInMilliseconds: Number = System.currentTimeMillis().toDouble()

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

                                measureLatency()

                                dms(this)
                                dm(this)
                                dd(this)
                                mgrs(this)
                                utm(this)

                                graphData(this)

                                if (MainPreferences.isCustomCoordinate()) {
                                    with(MainPreferences.getCoordinates()) {
                                        address(LatLng(this[0].toDouble(), this[1].toDouble()), false)
                                    }
                                } else {
                                    address(LatLng(latitude, longitude), false)
                                }

                                Log.d("LocationViewModel", "Location Posted")
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

    private fun measureLatency() {
        kotlin.runCatching {
            var currentLatency: Number

            with(System.currentTimeMillis()) {
                currentLatency = this.toDouble() - lastLatencyInMilliseconds.toDouble()
                lastLatencyInMilliseconds = this.toDouble()
            }

            val inSeconds: Boolean

            currentLatency = if (currentLatency.toDouble() > 999) {
                inSeconds = true
                MathExtensions.round(currentLatency.toDouble() / 1000.0, 3)
            } else {
                inSeconds = false
                currentLatency.toInt()
            }

            val str: Spannable = with(getApplication<Application>()) {
                fromHtml("<b>${getString(R.string.gps_latency)}</b> " +
                        "$currentLatency " +
                        if (inSeconds) getString(R.string.seconds) else getString(R.string.milliseconds)).toSpannable()
            }

            with(getApplication<Application>()) {
                if (inSeconds) {
                    if (currentLatency.toDouble() > 5.0) {
                        str.setSpan(ForegroundColorSpan(Color.RED), getString(R.string.gps_latency).length, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        latency.postValue(str)
                    } else {
                        latency.postValue(str)
                    }
                }
            }
        }.getOrElse {
            with(getApplication<Application>()) {
                latency.postValue(fromHtml("<b>${getString(R.string.gps_latency)}</b> " +
                        getString(R.string.not_available)).toSpannable())
            }
        }
    }

    private fun dms(location: Location) {
        with(getApplication<Application>()) {
            dms.postValue(Pair(
                    fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDMS(location.latitude, this)}"),
                    fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDMS(location.longitude, this)}")
            ))
        }
    }

    private fun dm(location: Location) {
        with(getApplication<Application>()) {
            dm.postValue(Pair(
                    fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDM(location.latitude, this)}"),
                    fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDM(location.longitude, this)}")
            ))
        }
    }

    private fun dd(location: Location) {
        with(getApplication<Application>()) {
            dd.postValue(Pair(
                    fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${latitudeAsDD(location.latitude)}"),
                    fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${longitudeAsDD(location.longitude)}")
            ))
        }
    }

    private fun mgrs(location: Location) {
        with(location) {
            mgrs.postValue(
                    MGRSCoord.fromLatLon(
                            Angle.fromDegreesLatitude(latitude),
                            Angle.fromDegreesLongitude(longitude)
                    ).toString())
        }
    }

    private fun utm(location: Location) {
        with(location) {
            utm.postValue(UTMConverter.getUTM(latitude, longitude))
        }
    }

    private fun graphData(location: Location) {
        accuracyGraphData.postValue(manipulateDataForGraph(accuracyData, location.accuracy))
        altitudeGraphData.postValue(manipulateDataForGraph(altitudeData, location.altitude.toFloat()))
    }

    private fun address(latLng: LatLng, override: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if(!override) {
                if (!this@LocationViewModel.address.hasActiveObservers()) return@launch
            }

            var address: String = getApplication<Application>().getString(R.string.not_available)

            runCatching {
                with(getApplication<Application>()) {
                    address = try {
                        if (!isNetworkAvailable(this)) {
                            getString(R.string.internet_connection_alert)
                        } else {
                            val geocoder = Geocoder(this, Locale.getDefault())

                            with(geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)) {
                                if (this != null && this.isNotEmpty()) {
                                    this[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                                } else {
                                    getString(R.string.not_available)
                                }
                            }
                        }
                    } catch (e: IOException) {
                        "${e.message}"
                    } catch (e: NullPointerException) {
                        "${e.message}\n${getString(R.string.no_address_found)}"
                    } catch (e: IllegalArgumentException) {
                        getString(R.string.invalid_coordinates)
                    }
                }
            }

            this@LocationViewModel.address.postValue(address)
        }
    }

    private fun manipulateDataForGraph(arrayList: ArrayList<Float>, value: Float): ArrayList<Float> {
        if (arrayList.isLastValueSame(value))
            return arrayList

        if (arrayList.size >= 45)
            arrayList.removeAt(0)

        arrayList.add(value)
        return arrayList
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(locationBroadcastReceiver)
    }
}
