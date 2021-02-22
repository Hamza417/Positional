package app.simple.positional.dialogs.gps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.math.MathExtensions
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.preference.MainPreferences
import app.simple.positional.sparkline.SparkLineLayout
import app.simple.positional.util.ArrayHelper.isLastValueSame
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.views.CustomBottomSheetDialogFragment

class LocationExpansion : CustomBottomSheetDialogFragment() {

    private var broadcastReceiver: BroadcastReceiver? = null
    private val broadcastFilter = IntentFilter()
    private val accuracyData = ArrayList<Float>()
    private val altitudeData = ArrayList<Float>()

    private lateinit var accuracyChart: SparkLineLayout
    private lateinit var altitudeChart: SparkLineLayout

    private lateinit var accuracyTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var accuracyInfoTextView: TextView
    private lateinit var altitudeInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_location, container, false)

        broadcastFilter.addAction("location")

        accuracyTextView = view.findViewById(R.id.location_accuracy_chart_text)
        altitudeTextView = view.findViewById(R.id.location_altitude_chart_text)
        accuracyInfoTextView = view.findViewById(R.id.location_accuracy_chart_data_text)
        altitudeInfoTextView = view.findViewById(R.id.location_altitude_chart_data_text)
        accuracyChart = view.findViewById(R.id.location_accuracy_chart)
        altitudeChart = view.findViewById(R.id.location_altitude_chart)

        accuracyChart.setData(manipulateDataForGraph(accuracyData, 0F))
        altitudeChart.setData(manipulateDataForGraph(altitudeData, 0F))

        return view
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver!!, broadcastFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "location") {
                    val location = intent.getParcelableExtra<Location>("location") ?: return

                    val accuracyData = manipulateDataForGraph(accuracyData, location.accuracy)
                    val altitudeData = manipulateDataForGraph(altitudeData, location.altitude.toFloat())

                    val accuracy: Spanned?
                    val altitude: Spanned?
                    val accuracyInfo = prepareGraphInformation(accuracyData)
                    val altitudeInfo = prepareGraphInformation(altitudeData)

                    if (MainPreferences.getUnit()) { // If unit is metric
                        accuracy = fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${MathExtensions.round(location.accuracy.toDouble(), 2)} ${getString(R.string.meter)}")
                        altitude = fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${MathExtensions.round(location.altitude, 2)} ${getString(R.string.meter)}")
                    } else { // else imperial
                        accuracy = fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${MathExtensions.round(location.accuracy.toDouble().toFeet(), 2)} ${getString(R.string.feet)}")
                        altitude = fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${MathExtensions.round(location.altitude.toFeet(), 2)} ${getString(R.string.feet)}")
                    }

                    accuracyChart.setData(accuracyData)
                    altitudeChart.setData(altitudeData)
                    accuracyTextView.text = accuracy
                    altitudeTextView.text = altitude
                    accuracyInfoTextView.text = accuracyInfo
                    altitudeInfoTextView.text = altitudeInfo
                }
            }
        }
    }

    private fun manipulateDataForGraph(arrayList: ArrayList<Float>, value: Float): ArrayList<Float> {
        if (arrayList.isLastValueSame(value))
            return arrayList

        if (arrayList.size >= 15)
            arrayList.removeAt(0)

        arrayList.add(value)
        return arrayList
    }

    private fun prepareGraphInformation(arrayList: ArrayList<Float>): Spanned {
        return fromHtml("<b>${getString(R.string.min)}</b> ${(arrayList.minOrNull() ?: 0).toDouble().formatToSmallerLengthUnit()}<br>" +
                "<b>${getString(R.string.max)}</b> ${(arrayList.maxOrNull() ?: 0).toDouble().formatToSmallerLengthUnit()}<br>" +
                "<b>${getString(R.string.avg)}</b> ${arrayList.average().formatToSmallerLengthUnit()}")
    }

    private fun Double.formatToSmallerLengthUnit(): String {
        return if (MainPreferences.getUnit()) {
            "${MathExtensions.round(this, 2)} ${getString(R.string.meter)}"
        } else {
            "${MathExtensions.round(this, 2).toFeet()} ${getString(R.string.feet)}"
        }
    }

    companion object {
        fun newInstance(): LocationExpansion {
            val args = Bundle()
            val fragment = LocationExpansion()
            fragment.arguments = args
            return fragment
        }
    }
}
