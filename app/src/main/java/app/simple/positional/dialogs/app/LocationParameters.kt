package app.simple.positional.dialogs.app

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.math.MathExtensions
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.NetworkCheck.isNetworkAvailable
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class LocationParameters : CustomBottomSheetDialogFragment() {
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var timezone: TextView
    private lateinit var address: TextView
    private lateinit var share: DynamicRippleButton

    private lateinit var locationViewModel: LocationViewModel
    private var location: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_custom_location_shortcut, container, false)

        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        timezone = view.findViewById(R.id.timezone)
        address = view.findViewById(R.id.address)
        share = view.findViewById(R.id.share)

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timezone.text = fromHtml("<b>${getString(R.string.local_timezone)}</b> " +
                ClockPreferences.getTimeZone())

        if (MainPreferences.isCustomCoordinate()) {
            latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " +
                    DMSConverter.getFormattedLatitude(MainPreferences.getCoordinates()[0].toDouble(), requireContext()))

            longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " +
                    DMSConverter.getFormattedLongitude(MainPreferences.getCoordinates()[1].toDouble(), requireContext()))

            address.text = fromHtml("<b>${getString(R.string.gps_address)}</b>: " +
                    MainPreferences.getAddress())
        } else {
            locationViewModel.getLocation().observe(viewLifecycleOwner) {
                location = it

                getAddress(LatLng(it.latitude, it.longitude))

                latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " +
                        DMSConverter.getFormattedLatitude(it.latitude, requireContext()))

                longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " +
                        DMSConverter.getFormattedLongitude(it.longitude, requireContext()))
            }
        }

        share.setOnClickListener {
            share()
        }
    }

    private fun getAddress(latLng: LatLng) {
        lifecycleScope.launch(Dispatchers.Default) {
            var address: String = getString(R.string.not_available)

            runCatching {
                address = try {
                    if (!isNetworkAvailable(requireContext())) {
                        getString(R.string.internet_connection_alert)
                    } else {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())

                        @Suppress("DEPRECATION")
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

            withContext(Dispatchers.Main) {
                this@LocationParameters.address.text =
                        fromHtml("<b>${getString(R.string.gps_address)}:</b> $address")
            }
        }
    }

    @Suppress("RemoveCurlyBracesFromTemplate")
    private fun share() {
        kotlin.runCatching {
            val latitude1 = MathExtensions.round(location?.latitude!!, 6)
            val longitude2 = MathExtensions.round(location?.longitude!!, 6)
            val link = "https://www.google.com/maps/search/?api=1&query=${latitude1},${longitude2}"
            val geoLink = "geo:${latitude1},${longitude2}?q=${latitude1},${longitude2}"
            val info = "${getString(R.string.gps_coordinates)}: ${latitude1}, ${longitude2}"
            val address = address.text.toString()

            val combined = "$info\n\n$address\n\nMaps:\n$link\n\nGeoLink:\n$geoLink"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, combined)
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }.getOrElse {
            Toast.makeText(requireContext(), it.message
                    ?: getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): LocationParameters {
            val args = Bundle()
            val fragment = LocationParameters()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "location_parameters"
    }
}
