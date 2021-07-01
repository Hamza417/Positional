package app.simple.positional.dialogs.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class CustomLocationParameters : CustomBottomSheetDialogFragment() {
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var timezone: TextView
    private lateinit var address: TextView
    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_custom_location_shortcut, container, false)

        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        timezone = view.findViewById(R.id.timezone)
        address = view.findViewById(R.id.address)

        filter.addAction("location")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timezone.text = fromHtml("<b>${getString(R.string.local_timezone)}</b> " +
                                         ClockPreferences.getTimeZone())

        if (MainPreferences.isCustomCoordinate()) {
            latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " +
                                             DMSConverter.latitudeAsDMS(MainPreferences.getCoordinates()[0].toDouble(), 3, requireContext()))

            longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " +
                                              DMSConverter.longitudeAsDMS(MainPreferences.getCoordinates()[1].toDouble(), 3, requireContext()))

            address.text = fromHtml("<b>${getString(R.string.gps_address)}</b>: " +
                                            MainPreferences.getAddress())
        } else {
            calculateAndUpdateData(MainPreferences.getLastCoordinates()[0].toDouble(),
                                   MainPreferences.getLastCoordinates()[1].toDouble())
        }

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            if (MainPreferences.isCustomCoordinate()) return
                            val location: Location = intent.getParcelableExtra("location") ?: return
                            calculateAndUpdateData(location.latitude, location.longitude)
                        }
                    }
                }
            }
        }
    }

    private fun calculateAndUpdateData(latitude: Double, longitude: Double) {
        this.latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " +
                                              DMSConverter.latitudeAsDMS(latitude, 3, requireContext()))

        this.longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " +
                                               DMSConverter.longitudeAsDMS(longitude, 3, requireContext()))

        getAddress(latitude, longitude)
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            var address: String = getString(R.string.not_available)

            withContext(Dispatchers.IO) {
                runCatching {
                    address = try {
                        if (context == null) {
                            getString(R.string.error)
                        } else if (!isNetworkAvailable(requireContext())) {
                            getString(R.string.internet_connection_alert)
                        } else {
                            val addresses: List<Address>
                            val geocoder = Geocoder(context, Locale.getDefault())

                            addresses = geocoder.getFromLocation(latitude, longitude, 1)

                            if (addresses != null && addresses.isNotEmpty()) {
                                addresses[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                            } else {
                                getString(R.string.not_available)
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

            try {
                this@CustomLocationParameters.address.text = fromHtml("<b>${getString(R.string.gps_address)}</b>: " +
                                                                              address)
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
    }

    companion object {
        fun newInstance(): CustomLocationParameters {
            val args = Bundle()
            val fragment = CustomLocationParameters()
            fragment.arguments = args
            return fragment
        }
    }
}