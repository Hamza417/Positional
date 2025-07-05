package app.simple.positional.adapters.measure

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.R.id
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_FOOTER
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_HEADER
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.model.Measure
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.LocationExtension
import com.github.vipulasri.timelineview.TimelineView

class AdapterMeasurePoints(private val measure: Measure) :
    RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            0, 1, 2, 3 -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_measure_point, parent, false), viewType)
            }
            TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_measure_point_header, parent, false))
            }
            TYPE_FOOTER -> {
                Footer(LayoutInflater.from(parent.context).inflate(R.layout.adapter_measure_footer, parent, false))
            }
            else       -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        when (holder) {
            is Holder -> {
                setMarker(holder, R.drawable.ic_point)
                val latlng = measure.measurePoints?.get(position)?.latLng!!
                holder.coordinates.text = with(holder.itemView.context) {
                    getString(R.string.coordinates_format,
                        DMSConverter.getFormattedLatitude(latlng.latitude, this),
                        DMSConverter.getFormattedLongitude(latlng.longitude, this))
                }
                val latLngArray = measure.measurePoints!!.map { it.latLng }.toTypedArray()
                holder.currentDistance.text = LocationExtension.measureDisplacement(latLngArray).toString()
            }
            is Header -> {
                holder.name.text = measure.name ?: "--"
                holder.total.text = measure.measurePoints?.size.toString()
                // holder.distance.text = getAccuracy(measure.totalDistance) + " " + getUnit(measure.totalDistance, holder.itemView.context)
            }
        }
    }

    override fun getItemCount(): Int {
        return measure.measurePoints?.size?.plus(2) ?: 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isPositionHeader(position) -> {
                TYPE_HEADER
            }
            isPositionFooter(position) -> {
                TYPE_FOOTER
            }
            else -> {
                TimelineView.getTimeLineViewType(position - 1, itemCount - 2)
            }
        }
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun isPositionFooter(position: Int): Boolean {
        return position + 1 == itemCount
    }

    private fun setMarker(holder: Holder, drawableResId: Int) {
        holder.timeline.marker = ContextCompat.getDrawable(holder.itemView.context, drawableResId)?.apply {
            setTint(ContextCompat.getColor(holder.itemView.context, R.color.iconColor))
        }
    }

    private fun getAccuracy(float: Float): String {
        return if (MainPreferences.isMetric()) {
            if (float < 1000F) {
                float.toString()
            } else {
                float.toKilometers().toString()
            }
        } else {
            if (float < 1000F) {
                float.toFeet().toString()
            } else {
                float.toMiles().toString()
            }
        }
    }

    private fun getUnit(float: Float, context: Context): String {
        return if (MainPreferences.isMetric()) {
            if (float < 1000F) {
                context.getString(R.string.meter)
            } else {
                context.getString(R.string.kilometer)
            }
        } else {
            if (float < 1000F) {
                context.getString(R.string.feet)
            } else {
                context.getString(R.string.miles)
            }
        }
    }

    inner class Holder(itemView: View, viewType: Int) : VerticalListViewHolder(itemView) {
        val timeline: TimelineView = itemView.findViewById(id.timeline)
        val coordinates: TextView = itemView.findViewById(id.coordinates)
        val currentDistance: TextView = itemView.findViewById(id.current_distance)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(id.container)

        init {
            timeline.initLine(viewType)
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(id.name)
        val total: TextView = itemView.findViewById(id.total_points)
        val distance: TextView = itemView.findViewById(id.total_distance)
    }

    inner class Footer(itemView: View) : VerticalListViewHolder(itemView)
}
