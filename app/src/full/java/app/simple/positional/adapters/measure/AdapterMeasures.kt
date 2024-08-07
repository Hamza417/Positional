package app.simple.positional.adapters.measure

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
import app.simple.positional.model.Measure
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.TimeFormatter.formatDate
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible

class AdapterMeasures(private val list: ArrayList<Measure>) :
    RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterMeasuresCallback: AdapterMeasuresCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            VerticalListViewHolder.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trails, parent, false))
            }
            VerticalListViewHolder.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_measures_header, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, holderPosition: Int) {
        val position = holderPosition.minus(1)

        if (holder is Holder) {
            holder.name.text = list[position].name
            holder.note.text = list[position].note
            holder.date.text = list[position].dateCreated.formatDate()

            if (holder.note.text.isEmpty()) {
                holder.note.gone(false)
            } else {
                holder.note.visible(false)
            }

            if (list[position].dateCreated == MeasurePreferences.getLastSelectedMeasure()) {
                holder.name.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))
            } else {
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textPrimary))
            }

            holder.menu.setOnClickListener {
                adapterMeasuresCallback.onMeasureMenuClicked(list[position], it, position)
            }

            holder.container.setOnClickListener {
                adapterMeasuresCallback.onMeasureClicked(list[position])
            }
        } else if (holder is Header) {
            holder.total.text = HtmlHelper.fromHtml("<b>${holder.itemView.context.getString(R.string.total)}</b> ${list.size}")
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position.isZero() -> VerticalListViewHolder.TYPE_HEADER
            else -> VerticalListViewHolder.TYPE_ITEM
        }
    }

    fun deleteMeasure(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position.plus(1))
        notifyItemChanged(0)
    }

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.trails_name)
        val note: TextView = itemView.findViewById(R.id.trails_note)
        val date: TextView = itemView.findViewById(R.id.trails_date)
        val menu: DynamicRippleImageButton = itemView.findViewById(R.id.menu)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_trails_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.total)
    }

    fun setOnAdapterMeasuresCallbackListener(adapterMeasuresCallback: AdapterMeasuresCallback) {
        this.adapterMeasuresCallback = adapterMeasuresCallback
    }

    companion object {
        interface AdapterMeasuresCallback {
            fun onMeasureClicked(measure: Measure)
            fun onMeasureMenuClicked(measure: Measure, view: View, position: Int)
        }
    }
}
