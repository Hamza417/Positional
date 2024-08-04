package app.simple.positional.adapters.trail

import android.content.Context
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.R.id
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_FOOTER
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_HEADER
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.model.TrailPoint
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.TimeFormatter.formatDate
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.gms.maps.model.LatLng

class AdapterTrailPoints(private val trailPoint: Pair<ArrayList<TrailPoint>, Triple<String?, Spanned?, Spanned?>>) :
    RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var trailsDataCallbacks: AdapterTrailsDataCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            0, 1, 2, 3  -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data, parent, false), viewType)
            }
            TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data_header, parent, false))
            }
            TYPE_FOOTER -> {
                Footer(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data_footer, parent, false))
            }
            else        -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            setMarker(holder,
                TrailIcons.icons[trailPoint.first[position].iconPosition])

            holder.name.text = trailPoint.first[position].name ?: "--"
            holder.note.text = trailPoint.first[position].note
                               ?: holder.itemView.context.getString(R.string.not_available)
            holder.date.text = trailPoint.first[position].timeAdded.formatDate()
            holder.coordinates.text = with(holder.itemView.context) {
                getString(R.string.coordinates_format,
                    DMSConverter.getFormattedLatitude(trailPoint.first[position].latitude, this),
                    DMSConverter.getFormattedLongitude(trailPoint.first[position].longitude, this))
            }

            holder.accuracy.text = holder.itemView.context.getString(
                R.string.trail_data_accuracy,
                getAccuracy(trailPoint.first[position].accuracy.round(1)),
                getUnit(trailPoint.first[position].accuracy, holder.itemView.context)
            )

            holder.container.setOnClickListener {
                with(trailPoint.first[position]) {
                    trailsDataCallbacks.onTrailClicked(LatLng(latitude, longitude))
                }
            }

            holder.container.setOnLongClickListener {
                trailsDataCallbacks.onTrailsDataLongPressed(trailPoint.first[position], it, position + 1)
                true
            }
        } else if (holder is Header) {
            holder.name.text = trailPoint.second.first
            holder.total.text = trailPoint.second.second
            holder.distance.text = trailPoint.second.third
            holder.add.setOnClickListener {
                trailsDataCallbacks.onAdd(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return trailPoint.first.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isPositionHeader(position) -> {
                TYPE_HEADER
            }
            isPositionFooter(position) -> {
                TYPE_FOOTER
            }
            else                       -> {
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
        holder.icon.marker = ContextCompat.getDrawable(holder.itemView.context, drawableResId)?.apply {
            setTint(ContextCompat.getColor(holder.itemView.context, R.color.iconColor))
        }
    }

    fun setOnTrailsDataCallbackListener(trailsDataCallbacks: AdapterTrailsDataCallbacks) {
        this.trailsDataCallbacks = trailsDataCallbacks
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
        val icon: TimelineView = itemView.findViewById(id.adapter_trail_data_timeline)
        val name: TextView = itemView.findViewById(id.adapter_trail_data_name)
        val note: TextView = itemView.findViewById(id.adapter_trail_data_notes)
        val coordinates: TextView = itemView.findViewById(id.adapter_trail_data_coordinates)
        val date: TextView = itemView.findViewById(id.adapter_trail_data_date)
        val accuracy: TextView = itemView.findViewById(id.adapter_trail_data_accuracy)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(id.adapter_trails_data_item_container)

        init {
            icon.initLine(viewType)
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(id.adapter_trail_data_trail_name)
        val total: TextView = itemView.findViewById(id.adapter_trail_data_trail_total)
        val distance: TextView = itemView.findViewById(id.adapter_trail_data_trail_distance)
        val add: DynamicRippleImageButton = itemView.findViewById(R.id.add)

        init {
            distance.isSelected = true
        }
    }

    inner class Footer(itemView: View) : VerticalListViewHolder(itemView)

    companion object {
        interface AdapterTrailsDataCallbacks {
            fun onTrailsDataLongPressed(trailPoint: TrailPoint, view: View, position: Int)
            fun onAdd(view: View)
            fun onTrailClicked(latLng: LatLng)
        }
    }
}
