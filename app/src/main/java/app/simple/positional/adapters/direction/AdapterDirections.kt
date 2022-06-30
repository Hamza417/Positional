package app.simple.positional.adapters.direction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.DirectionModel
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.TimeFormatter.formatDate
import com.google.android.gms.maps.model.LatLng

class AdapterDirections constructor(val list: MutableList<DirectionModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterDirectionsCallbacks: AdapterDirectionsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            VerticalListViewHolder.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_directions, parent, false))
            }
            VerticalListViewHolder.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_directions, parent, false))
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        if (holder is Holder) {
            val position = position_ - 1

            holder.name.text = list[position].name
            holder.latLng.text = DMSConverter.getDMS(LatLng(list[position].latitude, list[position].longitude), holder.itemView.context)
            holder.date.text = list[position].dateAdded.formatDate()

            holder.container.setOnClickListener {
                adapterDirectionsCallbacks?.onDirectionClicked(list[position])
            }

            holder.container.setOnLongClickListener {
                adapterDirectionsCallbacks?.onDirectionLongPressed(list[position])
                true
            }

            holder.menu.setOnClickListener {
                adapterDirectionsCallbacks?.onMenuPressed(list[position], it)
            }
        } else if (holder is Header) {
            holder.total.text = HtmlHelper.fromHtml("<b>${holder.itemView.context.getString(R.string.total)}</b> ${list.size}")
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            VerticalListViewHolder.TYPE_HEADER
        } else {
            VerticalListViewHolder.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.direction_name)
        val latLng: TextView = itemView.findViewById(R.id.direction_lat_lng)
        val date: TextView = itemView.findViewById(R.id.direction_date)
        val menu: DynamicRippleImageButton = itemView.findViewById(R.id.menu)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_direction_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total : TextView = itemView.findViewById(R.id.adapter_total)
    }

    fun setOnDirectionCallbacksListener(adapterDirectionsCallbacks: AdapterDirectionsCallbacks) {
        this.adapterDirectionsCallbacks = adapterDirectionsCallbacks
    }

    companion object {
        interface AdapterDirectionsCallbacks {
            fun onDirectionClicked(directionModel: DirectionModel)
            fun onDirectionLongPressed(directionModel: DirectionModel)
            fun onMenuPressed(directionModel: DirectionModel, view: View)
        }
    }
}