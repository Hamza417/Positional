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
import app.simple.positional.model.TrailEntry
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.TimeFormatter.formatDate

class AdapterTrails(private val list: ArrayList<TrailEntry>) :
    RecyclerView.Adapter<VerticalListViewHolder>() {

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

            if (list[position].trailName == TrailPreferences.getCurrentTrail()) {
                holder.name.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))
            } else {
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textPrimary))
            }

            holder.menu.setOnClickListener {
                adapterTrailsCallback.onTrailMenu(list[position], it)
            }

            holder.container.setOnClickListener {
                adapterTrailsCallback.onTrailClicked(list[position].trailName)
            }

        } else if (holder is Header) {
            holder.total.text = HtmlHelper.fromHtml("<b>${holder.itemView.context.getString(R.string.total)}</b> ${list.size}")
        }
    }

    override fun getItemCount(): Int {
        return if(list.isNullOrEmpty()) {
            0
        } else {
            list.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            VerticalListViewHolder.TYPE_HEADER
        } else VerticalListViewHolder.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.trails_name)
        val note: TextView = itemView.findViewById(R.id.trails_note)
        val date: TextView = itemView.findViewById(R.id.trails_date)
        val menu: DynamicRippleImageButton = itemView.findViewById(R.id.menu)
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
            fun onTrailMenu(trailEntry: TrailEntry, anchor: View)
        }
    }
}
