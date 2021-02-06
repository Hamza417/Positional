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
import app.simple.positional.math.MathExtensions
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.preference.MainPreferences
import app.simple.positional.sparkline.SparkLineLayout
import app.simple.positional.util.HtmlHelper
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LocationExpansion : CustomBottomSheetDialog() {

    private var broadcastReceiver: BroadcastReceiver? = null
    private val broadcastFilter = IntentFilter()
    private val accuracyData = ArrayList<Float>()
    private val altitudeData = ArrayList<Float>()

    private lateinit var accuracyChart: SparkLineLayout
    private lateinit var altitudeChart: SparkLineLayout

    private lateinit var accuracyTextView: TextView
    private lateinit var altitudeTextView: TextView

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

                    CoroutineScope(Dispatchers.Default).launch {
                        val accuracy = if (MainPreferences.getUnit()) {
                            HtmlHelper.fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${MathExtensions.round(location.accuracy.toDouble(), 2)} ${getString(R.string.meter)}")
                        } else {
                            HtmlHelper.fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${MathExtensions.round(location.accuracy.toDouble().toFeet(), 2)} ${getString(R.string.feet)}")
                        }

                        val altitude = if (MainPreferences.getUnit()) {
                            HtmlHelper.fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${MathExtensions.round(location.altitude, 2)} ${getString(R.string.meter)}")
                        } else {
                            HtmlHelper.fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${MathExtensions.round(location.altitude.toFeet(), 2)} ${getString(R.string.feet)}")
                        }

                        val accuracyData = manipulateDataForGraph(accuracyData, location.accuracy)
                        val altitudeData = manipulateDataForGraph(altitudeData, location.altitude.toFloat())

                        withContext(Dispatchers.Main) {
                            accuracyChart.setData(accuracyData)
                            altitudeChart.setData(altitudeData)
                            accuracyTextView.text = accuracy
                            altitudeTextView.text = altitude
                        }
                    }
                }
            }
        }
    }

    private fun manipulateDataForGraph(arrayList: ArrayList<Float>, value: Float): ArrayList<Float> {
        println(compensateValue(value))
        if (arrayList.size >= 15) {
            arrayList.removeAt(0)
        }
        arrayList.add(compensateValue(value))
        return ArrayList(arrayList)
    }

    private fun compensateValue(value: Float): Float {
        /*
         * The random number is here as a workaround for a bug in graph view
         * which makes the graph disappear if the value remains constant for
         * more than 15 times.
         *
         * The random number will add very tiny non significant value to
         * make the graph working without causing too many inaccuracies
         *
         * TODO - fix graph bug
         */
        return value + Random().nextFloat() * (0.005f + 0f) - 0.005f
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