package app.simple.positional.adapters.trail

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
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_FOOTER
import app.simple.positional.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_HEADER
import app.simple.positional.model.TrailData
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.TimeFormatter.formatDate
import com.github.vipulasri.timelineview.TimelineView

class AdapterTrailData(private val trailData: Pair<ArrayList<TrailData>, Triple<String?, Spanned?, Spanned?>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var trailsDataCallbacks: AdapterTrailsDataCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            0, 1, 2, 3 -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data, parent, false), viewType)
            }
            TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data_header, parent, false))
            }
            TYPE_FOOTER -> {
                Footer(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data_footer, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            setMarker(holder,
                      TrailIcons.icons[trailData.first[position].iconPosition])

            holder.name.text = trailData.first[position].name ?: "--"
            holder.note.text = trailData.first[position].note ?: holder.itemView.context.getString(R.string.not_available)
            holder.date.text = trailData.first[position].timeAdded.formatDate()
            holder.coordinates.text = with(holder.itemView.context) {
                getString(R.string.coordinates_format,
                          DMSConverter.latitudeAsDMS(trailData.first[position].latitude, 2, this),
                          DMSConverter.longitudeAsDMS(trailData.first[position].longitude, 2, this))
            }

            holder.container.setOnLongClickListener {
                trailsDataCallbacks.onTrailsDataLongPressed(trailData.first[position], it, position + 1)
                true
            }
        } else if (holder is Header) {
            holder.name.text = trailData.second.first
            holder.total.text = trailData.second.second
            holder.distance.text = trailData.second.third
        }
    }

    override fun getItemCount(): Int {
        return trailData.first.size + 2
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
        holder.icon.marker = ContextCompat.getDrawable(holder.itemView.context, drawableResId)
    }

    fun setOnTrailsDataCallbackListener(trailsDataCallbacks: AdapterTrailsDataCallbacks) {
        this.trailsDataCallbacks = trailsDataCallbacks
    }

    fun removeItem(position: Int) {
        trailData.first.removeAt(position - 1)
        notifyItemRemoved(position)
    }

    inner class Holder(itemView: View, viewType: Int) : VerticalListViewHolder(itemView) {
        val icon: TimelineView = itemView.findViewById(id.adapter_trail_data_timeline)
        val name: TextView = itemView.findViewById(id.adapter_trail_data_name)
        val note: TextView = itemView.findViewById(id.adapter_trail_data_notes)
        val coordinates: TextView = itemView.findViewById(id.adapter_trail_data_coordinates)
        val date: TextView = itemView.findViewById(id.adapter_trail_data_date)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_trails_data_item_container)

        init {
            icon.initLine(viewType)
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(id.adapter_trail_data_trail_name)
        val total: TextView = itemView.findViewById(id.adapter_trail_data_trail_total)
        val distance: TextView = itemView.findViewById(id.adapter_trail_data_trail_distance)

        init {
            distance.isSelected = true
        }
    }

    inner class Footer(itemView: View) : VerticalListViewHolder(itemView)

    companion object {
        interface AdapterTrailsDataCallbacks {
            fun onTrailsDataLongPressed(trailData: TrailData, view: View, i: Int)
        }
    }
}