package app.simple.positional.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.util.LocationConverter
import com.elyeproj.loaderviewlibrary.LoaderTextView
import java.util.*


class GPS : Fragment(), LocationListener {

    lateinit var range: ImageView
    lateinit var dot: ImageView
    lateinit var scanned: ImageView

    private lateinit var accuracy: LoaderTextView
    lateinit var address: LoaderTextView
    lateinit var latitude: LoaderTextView
    lateinit var longitude: LoaderTextView

    lateinit var locationManager: LocationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)
        range = view.findViewById(R.id.gps_range_indicator)
        dot = view.findViewById(R.id.gps_dot)
        scanned = view.findViewById(R.id.gps_scanned_indicator)

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location != null) {
                    onLocationChanged(location)
                }
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1f, this@GPS)

        return view
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            println(location.accuracy)

            accuracy.text = "${location.accuracy.toString().substring(0, 4)} m"

            location.latitude = -28.425751
            location.longitude = 134.239923

            getAddress(location.latitude, location.longitude)

            val scaled = 0.066f * location.accuracy

            if (scaled < 1.0f) {
                changeRangeSize(scaled)
            } else {
                changeRangeSize(1.0f)
            }

            latitude.text = Html.fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(location.latitude, 3)}")
            longitude.text = Html.fromHtml("<b>Longitude:</b> ${LocationConverter.latitudeAsDMS(location.longitude, 3)}°")

            if (context != null) {
                val animIn: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.gps_scanned_animation)
                scanned.startAnimation(animIn)
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1f, this@GPS)
    }

    override fun onProviderDisabled(provider: String?) {
        accuracy.resetLoader()
    }

    private fun changeRangeSize(value: Float) {
        println(value)
        range.animate().scaleX(value).scaleY(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        class GetAddress : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {

                if (context == null) {
                    return ""
                }

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
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                address.text = result
            }
        }

        val getAddress = GetAddress()
        if (getAddress.status == AsyncTask.Status.RUNNING) {
            getAddress.cancel(true)
        } else {
            getAddress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}