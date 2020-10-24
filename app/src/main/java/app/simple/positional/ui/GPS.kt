package app.simple.positional.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.util.LocationConverter
import app.simple.positional.util.isNetworkAvailable
import app.simple.positional.util.round
import com.elyeproj.loaderviewlibrary.LoaderTextView
import java.io.IOException
import java.util.*


class GPS : Fragment() {

    lateinit var range: ImageView
    lateinit var dot: ImageView
    lateinit var scanned: ImageView

    private lateinit var accuracy: LoaderTextView
    lateinit var address: LoaderTextView
    lateinit var latitude: LoaderTextView
    lateinit var longitude: LoaderTextView
    lateinit var provider: LoaderTextView
    lateinit var altitude: LoaderTextView
    lateinit var bearing: LoaderTextView
    lateinit var speed: LoaderTextView
    lateinit var handler: Handler
    private var filter: IntentFilter = IntentFilter("location_update")
    private lateinit var locationBroadcastReceiver: BroadcastReceiver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)
        range = view.findViewById(R.id.gps_range_indicator)
        dot = view.findViewById(R.id.gps_dot)
        scanned = view.findViewById(R.id.gps_scanned_indicator)

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        speed = view.findViewById(R.id.gps_speed)
        altitude = view.findViewById(R.id.gps_altitude)
        bearing = view.findViewById(R.id.gps_bearing)
        provider = view.findViewById(R.id.gps_provider)

        handler = Handler()
        handler.post(repeatAnimation)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    val location: Location? = intent.getParcelableExtra("location")
                    if (location != null) {
                        altitude.text = "${round(location.altitude, 2)} m"
                        speed.text = "${location.speed}"
                        bearing.text = "${location.bearing}"
                        provider.text = location.provider.toUpperCase(Locale.getDefault())
                        accuracy.text = "${round(location.accuracy.toDouble(), 2)} m"

                        //location.latitude = -28.425751
                        //location.longitude = 134.239923

                        getAddress(location.latitude, location.longitude)

                        val scaled = 0.066f * location.accuracy

                        if (scaled < 1.0f) {
                            changeRangeSize(scaled)
                        } else {
                            changeRangeSize(1.0f)
                        }

                        latitude.text = Html.fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(location.latitude, 3)}")
                        longitude.text = Html.fromHtml("<b>Longitude:</b> ${LocationConverter.latitudeAsDMS(location.longitude, 3)}°")
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scanned.clearAnimation()
        range.clearAnimation()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(repeatAnimation)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
        handler.post(repeatAnimation)
    }

    private fun changeRangeSize(value: Float) {
        range.animate().scaleX(value).scaleY(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        class GetAddress : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {

                if (context == null) {
                    return ""
                }

                if (!isNetworkAvailable(requireContext())) {
                    return "Internet connection not available"
                }

                try {
                    val addresses: List<Address>
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    val address: String = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    val city: String = addresses[0].locality
                    val state: String = addresses[0].adminArea
                    val country: String = addresses[0].countryName
                    val postalCode: String = addresses[0].postalCode
                    val knownName: String = addresses[0].featureName // Only if available else return NULL

                    return address //"$city, $state, $country, $postalCode, $knownName"
                } catch (e: IOException) {
                    e.printStackTrace()
                    return "${e.message}\n!Error Fetching Address"
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                address.text = result
            }
        }

        val getAddress = GetAddress()
        if (getAddress.status == AsyncTask.Status.RUNNING) {
            if (getAddress.cancel(true)) {
                getAddress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        } else {
            getAddress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    private val repeatAnimation: Runnable = object : Runnable {
        override fun run() {
            if (context == null) return
            val animIn: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.gps_scanned_animation)
            scanned.startAnimation(animIn)
            handler.postDelayed(this, 4000)
        }
    }
}