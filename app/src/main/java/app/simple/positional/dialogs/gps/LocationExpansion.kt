package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.math.MathExtensions
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.sparkline.view.SparkLineLayout
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.viewmodels.viewmodel.LocationViewModel

class LocationExpansion : CustomBottomSheetDialogFragment() {

    private lateinit var locationViewModel: LocationViewModel

    private lateinit var accuracyChart: SparkLineLayout
    private lateinit var altitudeChart: SparkLineLayout

    private lateinit var accuracyTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var accuracyInfoTextView: TextView
    private lateinit var altitudeInfoTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_location, container, false)

        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        accuracyTextView = view.findViewById(R.id.location_accuracy_chart_text)
        altitudeTextView = view.findViewById(R.id.location_altitude_chart_text)
        accuracyInfoTextView = view.findViewById(R.id.location_accuracy_chart_data_text)
        altitudeInfoTextView = view.findViewById(R.id.location_altitude_chart_data_text)
        accuracyChart = view.findViewById(R.id.location_accuracy_chart)
        altitudeChart = view.findViewById(R.id.location_altitude_chart)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationViewModel.getLocation().observe(viewLifecycleOwner) {
            if (MainPreferences.isMetric()) { // Metric
                accuracyTextView.text = fromHtml("<b>${getString(R.string.gps_accuracy)}</b> " +
                        "${MathExtensions.round(it.accuracy.toDouble(), 2)} ${getString(R.string.meter)}")

                altitudeTextView.text = fromHtml("<b>${getString(R.string.gps_altitude)}</b> " +
                        "${MathExtensions.round(it.altitude, 2)} ${getString(R.string.meter)}")

            } else { // Imperial

                accuracyTextView.text = fromHtml("<b>${getString(R.string.gps_accuracy)}</b> " +
                        "${
                            MathExtensions.round(it.accuracy.toDouble().toFeet(), 2)
                        } ${getString(R.string.feet)}")
                altitudeTextView.text = fromHtml("<b>${getString(R.string.gps_altitude)}</b> " +
                        "${MathExtensions.round(it.altitude.toFeet(), 2)} ${getString(R.string.feet)}")
            }
        }

        locationViewModel.altitudeGraphData.observe(viewLifecycleOwner, {
            altitudeChart.setData(it)
            altitudeInfoTextView.text = prepareGraphInformation(it)
        })

        locationViewModel.accuracyGraphData.observe(viewLifecycleOwner, {
            accuracyChart.setData(it)
            accuracyInfoTextView.text = prepareGraphInformation(it)
        })
    }

    private fun prepareGraphInformation(arrayList: ArrayList<Float>): Spanned {
        return fromHtml("<b>${getString(R.string.min)}</b> ${(arrayList.minOrNull() ?: 0).toDouble().formatToSmallerLengthUnit()}<br>" +
                "<b>${getString(R.string.max)}</b> ${(arrayList.maxOrNull() ?: 0).toDouble().formatToSmallerLengthUnit()}<br>" +
                "<b>${getString(R.string.avg)}</b> ${arrayList.average().formatToSmallerLengthUnit()}")
    }

    private fun Double.formatToSmallerLengthUnit(): String {
        return if (MainPreferences.isMetric()) {
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
