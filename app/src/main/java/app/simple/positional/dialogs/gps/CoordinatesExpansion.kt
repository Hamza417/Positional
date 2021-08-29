package app.simple.positional.dialogs.gps

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.TextViewUtils.setTextAnimation
import app.simple.positional.util.UTMConverter
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoordinatesExpansion : CustomBottomSheetDialogFragment() {

    private lateinit var locationViewModel: LocationViewModel
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var coordinatesDataTextView: TextView
    private lateinit var dmsLatitude: TextView
    private lateinit var dmsLongitude: TextView
    private lateinit var dmLatitude: TextView
    private lateinit var dmLongitude: TextView
    private lateinit var ddLatitude: TextView
    private lateinit var ddLongitude: TextView
    private lateinit var mgrsCoordinates: TextView
    private lateinit var utmZone: TextView
    private lateinit var utmEasting: TextView
    private lateinit var utmNorthing: TextView
    private lateinit var utmMeridian: TextView

    private lateinit var copyImageButton: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_coordinates, container, false)

        coordinatesDataTextView = view.findViewById(R.id.coordinated_details_text)
        dmsLatitude = view.findViewById(R.id.dms_latitude)
        dmsLongitude = view.findViewById(R.id.dms_longitude)
        dmLatitude = view.findViewById(R.id.dm_latitude)
        dmLongitude = view.findViewById(R.id.dm_longitude)
        ddLatitude = view.findViewById(R.id.dd_latitude)
        ddLongitude = view.findViewById(R.id.dd_longitude)
        mgrsCoordinates = view.findViewById(R.id.mgrs_coordinates)
        utmZone = view.findViewById(R.id.utm_zone)
        utmEasting = view.findViewById(R.id.utm_easting)
        utmNorthing = view.findViewById(R.id.utm_northing)
        utmMeridian = view.findViewById(R.id.utm_meridian)

        copyImageButton = view.findViewById(R.id.coordinates_copy)

        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        copyImageButton.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val stringBuilder = StringBuilder()

            stringBuilder.append("DD°MM'SS.SSS\n")
            stringBuilder.append("${dmsLatitude.text}\n")
            stringBuilder.append("${dmsLongitude.text}\n")
            stringBuilder.append("\n")
            stringBuilder.append("DD°MM.MMM'\n")
            stringBuilder.append("${dmLatitude.text}\n")
            stringBuilder.append("${dmLongitude.text}\n")
            stringBuilder.append("\n")
            stringBuilder.append("DD.DDD°\n")
            stringBuilder.append("${ddLatitude.text}\n")
            stringBuilder.append("${ddLongitude.text}\n")
            stringBuilder.append("\n")
            stringBuilder.append("MGRS\n")
            stringBuilder.append("${mgrsCoordinates.text}\n")
            stringBuilder.append("\n")
            stringBuilder.append("UTM\n")
            stringBuilder.append("${utmZone.text}\n")
            stringBuilder.append("${utmEasting.text}\n")
            stringBuilder.append("${utmNorthing.text}\n")
            stringBuilder.append("${utmMeridian.text}\n")

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("\n\n")
                stringBuilder.append("Information is copied using Positional Lite\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
            }

            val clip: ClipData = ClipData.newPlainText("Coordinates Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                coordinatesDataTextView.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }

        if (MainPreferences.isCustomCoordinate()) {
            with(MainPreferences.getCoordinates()) {
                formatCoordinates(this[0].toDouble(), this[1].toDouble())
            }

            return
        }

        locationViewModel.dms.observe(viewLifecycleOwner, {
            dmsLatitude.text = it.first
            dmsLongitude.text = it.second
        })

        locationViewModel.dm.observe(viewLifecycleOwner, {
            dmLatitude.text = it.first
            dmLongitude.text = it.second
        })

        locationViewModel.dd.observe(viewLifecycleOwner, {
            ddLatitude.text = it.first
            ddLongitude.text = it.second
        })

        locationViewModel.mgrs.observe(viewLifecycleOwner, {
            mgrsCoordinates.text = it
        })

        locationViewModel.utm.observe(viewLifecycleOwner, {
            utmZone.text = fromHtml("<b>${getString(R.string.utm_zone)}</b> ${it.zone}")
            utmEasting.text = fromHtml("<b>${getString(R.string.utm_easting)}</b> ${it.easting}")
            utmNorthing.text = fromHtml("<b>${getString(R.string.utm_northing)}</b> ${it.northing}")
            utmMeridian.text = fromHtml("<b>${getString(R.string.utm_meridian)}</b> ${it.centralMeridian}")
        })
    }

    override fun onDestroy() {
        handler.removeCallbacks(textAnimationRunnable)
        coordinatesDataTextView.clearAnimation()
        super.onDestroy()
    }

    private fun formatCoordinates(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            val dmsLatitude: Spanned
            val dmsLongitude: Spanned
            val dmLatitude: Spanned
            val dmLongitude: Spanned
            val ddLatitude: Spanned
            val ddLongitude: Spanned
            val mgrsCoord: String
            val utmZone: Spanned
            val utmEasting: Spanned
            val utmNorthing: Spanned
            val utmMeridian: Spanned

            withContext(Dispatchers.Default) {
                dmsLatitude = fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDMS(latitude, requireContext())}")
                dmsLongitude = fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDMS(longitude, requireContext())}")
                dmLatitude = fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDM(latitude, requireContext())}")
                dmLongitude = fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDM(longitude, requireContext())}")
                ddLatitude = fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDD(latitude)}")
                ddLongitude = fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDD(longitude)}")
                mgrsCoord = MGRSCoord.fromLatLon(Angle.fromDegreesLatitude(latitude), Angle.fromDegreesLongitude(longitude)).toString()

                val utm = UTMConverter.getUTM(latitude, longitude)
                utmZone = fromHtml("<b>${getString(R.string.utm_zone)}</b> ${utm.zone}")
                utmEasting = fromHtml("<b>${getString(R.string.utm_easting)}</b> ${utm.easting}")
                utmNorthing = fromHtml("<b>${getString(R.string.utm_northing)}</b> ${utm.northing}")
                utmMeridian = fromHtml("<b>${getString(R.string.utm_meridian)}</b> ${utm.centralMeridian}")
            }

            this@CoordinatesExpansion.dmsLatitude.text = dmsLatitude
            this@CoordinatesExpansion.dmsLongitude.text = dmsLongitude
            this@CoordinatesExpansion.mgrsCoordinates.text = mgrsCoord
            this@CoordinatesExpansion.utmZone.text = utmZone
            this@CoordinatesExpansion.utmEasting.text = utmEasting
            this@CoordinatesExpansion.utmNorthing.text = utmNorthing
            this@CoordinatesExpansion.utmMeridian.text = utmMeridian
            this@CoordinatesExpansion.dmLatitude.text = dmLatitude
            this@CoordinatesExpansion.dmLongitude.text = dmLongitude
            this@CoordinatesExpansion.ddLatitude.text = ddLatitude
            this@CoordinatesExpansion.ddLongitude.text = ddLongitude
        }
    }

    private val textAnimationRunnable = Runnable {
        coordinatesDataTextView.setTextAnimation(getString(R.string.gps_coordinates), 300)
    }

    companion object {
        fun newInstance(): CoordinatesExpansion {
            val args = Bundle()
            val fragment = CoordinatesExpansion()
            fragment.arguments = args
            return fragment
        }
    }
}
