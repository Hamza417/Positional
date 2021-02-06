package app.simple.positional.dialogs.gps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.NullSafety.isNull
import app.simple.positional.util.UTMConverter
import app.simple.positional.views.CustomBottomSheetDialog
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoordinatesExpansion : CustomBottomSheetDialog() {

    private lateinit var broadcastReceiver: BroadcastReceiver

    private lateinit var dmsLatitude: TextView
    private lateinit var dmsLongitude: TextView
    private lateinit var dmLatitude: TextView
    private lateinit var dmLongitude: TextView
    private lateinit var ddLatitude: TextView
    private lateinit var ddLongitude: TextView
    private lateinit var mgrsCoordinates: TextView
    private lateinit var utmCoordinates: TextView

    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_coordinates, container, false)

        dmsLatitude = view.findViewById(R.id.dms_latitude)
        dmsLongitude = view.findViewById(R.id.dms_longitude)
        dmLatitude = view.findViewById(R.id.dm_latitude)
        dmLongitude = view.findViewById(R.id.dm_longitude)
        ddLatitude = view.findViewById(R.id.dd_latitude)
        ddLongitude = view.findViewById(R.id.dd_longitude)
        mgrsCoordinates = view.findViewById(R.id.mgrs_coordinates)
        utmCoordinates = view.findViewById(R.id.utm_coordinates)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        formatCoordinates(requireArguments().getDouble("latitude"), requireArguments().getDouble("longitude"))
        if (MainPreferences.isCustomCoordinate()) return

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == "location") {
                    val location = intent.getParcelableExtra<Location>("location") ?: return
                    formatCoordinates(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!MainPreferences.isCustomCoordinate()) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, IntentFilter("location"))
        }
    }

    override fun onPause() {
        super.onPause()
        if (!MainPreferences.isCustomCoordinate()) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.Default).launch {
            if (context.isNull()) return@launch

            try {
                val dmsLatitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDMS(latitude, 3, requireContext())}")
                val dmsLongitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDMS(longitude, 3, requireContext())}")
                val dmLatitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.getLatitudeAsDM(latitude, requireContext())}")
                val dmLongitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.getLongitudeAsDM(longitude, requireContext())}")
                val ddLatitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.getLatitudeAsDD(latitude, requireContext())}")
                val ddLongitude = HtmlHelper.fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.getLongitudeAsDD(longitude, requireContext())}")
                val mgrsCoord = MGRSCoord.fromLatLon(Angle.fromDegreesLatitude(latitude), Angle.fromDegreesLongitude(longitude)).toString()
                val utmCoord = UTMConverter.getUTMCoordinates(latitude, longitude)

                withContext(Dispatchers.Main) {
                    this@CoordinatesExpansion.dmsLatitude.text = dmsLatitude
                    this@CoordinatesExpansion.dmsLongitude.text = dmsLongitude
                    this@CoordinatesExpansion.mgrsCoordinates.text = mgrsCoord
                    this@CoordinatesExpansion.utmCoordinates.text = utmCoord
                    this@CoordinatesExpansion.dmLatitude.text = dmLatitude
                    this@CoordinatesExpansion.dmLongitude.text = dmLongitude
                    this@CoordinatesExpansion.ddLatitude.text = ddLatitude
                    this@CoordinatesExpansion.ddLongitude.text = ddLongitude
                }
            } catch (ignored: IllegalStateException) {
            }
        }
    }

    companion object {
        fun newInstance(latitude: Double, longitude: Double): CoordinatesExpansion {
            val args = Bundle()
            args.putDouble("latitude", latitude)
            args.putDouble("longitude", longitude)
            val fragment = CoordinatesExpansion()
            fragment.arguments = args
            return fragment
        }
    }
}
