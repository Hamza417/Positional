package app.simple.positional.adapters.trail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.TrailModel
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.TimeFormatter.formatDate
import java.util.*

class AdapterTrails(private val list: ArrayList<TrailModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterTrailsCallback: AdapterTrailsCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            VerticalListViewHolder.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trails, parent, false))
            }
            VerticalListViewHolder.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trails_header, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            holder.name.text = list[position].trailName
            holder.note.text = list[position].trailNote
            holder.date.text = list[position].dateCreated.formatDate()

            if (list[position].trailName == TrailPreferences.getLastUsedTrail()) {
                holder.name.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))
            } else {
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textPrimary))
            }

            holder.delete.setOnClickListener {
                adapterTrailsCallback.onDelete(list[position], it)
            }

            holder.container.setOnClickListener {
                adapterTrailsCallback.onTrailClicked(list[position].trailName)
            }
        } else if (holder is Header) {
            holder.total.text = HtmlHelper.fromHtml("<b>${holder.itemView.context.getString(R.string.total)}</b> ${list.size}")
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) {
            VerticalListViewHolder.TYPE_HEADER
        } else VerticalListViewHolder.TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.trails_name)
        val note: TextView = itemView.findViewById(R.id.trails_note)
        val date: TextView = itemView.findViewById(R.id.trails_date)
        val delete: DynamicRippleImageButton = itemView.findViewById(R.id.delete)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_trails_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.adapter_trails_total)
    }

    fun setOnAdapterTrailsCallbackListener(adapterTrailsCallback: AdapterTrailsCallback) {
        this.adapterTrailsCallback = adapterTrailsCallback
    }

    companion object {
        interface AdapterTrailsCallback {
            fun onTrailClicked(name: String)
            fun onDelete(trailModel: TrailModel, anchor: View)
        }
    }
}