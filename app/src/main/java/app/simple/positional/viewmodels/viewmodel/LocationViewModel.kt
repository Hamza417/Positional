package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.text.Spanned
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.extensions.viewmodel.WrappedViewModel
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ArrayHelper.isLastValueSame
import app.simple.positional.util.DMSConverter.latitudeAsDD
import app.simple.positional.util.DMSConverter.latitudeAsDM
import app.simple.positional.util.DMSConverter.latitudeAsDMS
import app.simple.positional.util.DMSConverter.longitudeAsDD
import app.simple.positional.util.DMSConverter.longitudeAsDM
import app.simple.positional.util.DMSConverter.longitudeAsDMS
import app.simple.positional.util.Direction
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.UTMConverter
import com.google.android.gms.maps.model.LatLng
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * This viewmodel receives the location updated by location services
 * and updates the UI in the observing views. The only reason this
 * viewmodel is to prevent loss of location data between switching
 * fragments.
 */
class LocationViewModel(application: Application) : WrappedViewModel(application) {

    private var filter: IntentFilter = IntentFilter()
    private var locationBroadcastReceiver: BroadcastReceiver

    private val accuracyData = ArrayList<Float>()
    private val altitudeData = ArrayList<Float>()

    val location = MutableLiveData<Location>()
    val provider = MutableLiveData<String>()

    val dms = MutableLiveData<Pair<String, String>>()
    val dm = MutableLiveData<Pair<String, String>>()
    val dd = MutableLiveData<Pair<String, String>>()
    val mgrs = MutableLiveData<String>()
    val utm = MutableLiveData<UTMConverter.UTM>()
    val latency = MutableLiveData<Pair<Number, Boolean>>()
    val targetDisplacement = MutableLiveData<Spanned>()
    val targetDirection = MutableLiveData<Spanned>()

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

                                if (GPSPreferences.isTargetMarkerSet()) {
                                    targetData(
                                            LatLng(GPSPreferences.getTargetMarkerCoordinates()[0].toDouble(),
                                                    GPSPreferences.getTargetMarkerCoordinates()[1].toDouble()),
                                            this)
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

        LocalBroadcastManager.getInstance(getApplication())
            .registerReceiver(locationBroadcastReceiver, filter)
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
                round(currentLatency.toDouble() / 1000.0, 3)
            } else {
                inSeconds = false
                currentLatency.toInt()
            }

            latency.postValue(Pair(currentLatency, inSeconds))
        }
    }

    fun targetData(target: LatLng, current: Location) {
        targetDisplacement(target, LatLng(current.latitude, current.longitude))
        targetBearing(target, current)
    }

    private fun targetDisplacement(target: LatLng, current: LatLng) {
        viewModelScope.launch(Dispatchers.Default) {
            val builder = StringBuilder().also {
                it.append("<b>${getString(R.string.gps_displacement)} </b>")

                val p0 = LocationExtension.measureDisplacement(arrayOf(target, current))

                if (MainPreferences.getUnit()) {
                    if (p0 < 1000) {
                        it.append(p0.round(2))
                        it.append(" ")
                        it.append(getContext().getString(R.string.meter))
                    } else {
                        it.append(p0.toKilometers().round(2))
                        it.append(" ")
                        it.append(getContext().getString(R.string.kilometer))
                    }
                } else {
                    if (p0 < 1000) {
                        it.append(p0.toDouble().toFeet().toFloat().round(2))
                        it.append(" ")
                        it.append(getContext().getString(R.string.feet))
                    } else {
                        it.append(p0.toMiles().round(2))
                        it.append(" ")
                        it.append(getContext().getString(R.string.miles))
                    }
                }
            }

            this@LocationViewModel.targetDisplacement.postValue(HtmlHelper.fromHtml(builder.toString()))
        }
    }

    private fun targetBearing(target: LatLng, current: Location) {
        viewModelScope.launch(Dispatchers.Default) {
            val p0 = LocationExtension.calculateBearingAngle(
                    current.latitude,
                    current.longitude,
                    target.latitude,
                    target.longitude
            )

            val builder = StringBuilder().also {
                it.append("<b>${getString(R.string.gps_direction)} </b>")
                it.append(Direction.getDirectionNameFromAzimuth(getContext(), abs(p0)))
            }

            targetDirection.postValue(HtmlHelper.fromHtml(builder.toString()))
        }
    }

    private fun dms(location: Location) {
        with(getApplication<Application>()) {
            dms.postValue(Pair(
                    latitudeAsDMS(location.latitude, this),
                    longitudeAsDMS(location.longitude, this)))
        }
    }

    private fun dm(location: Location) {
        with(getApplication<Application>()) {
            dm.postValue(Pair(
                    latitudeAsDM(location.latitude, this),
                    longitudeAsDM(location.longitude, this)))
        }
    }

    private fun dd(location: Location) {
        dd.postValue(Pair(
                latitudeAsDD(location.latitude),
                longitudeAsDD(location.longitude)))
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
        LocalBroadcastManager.getInstance(getApplication())
            .unregisterReceiver(locationBroadcastReceiver)
    }
}