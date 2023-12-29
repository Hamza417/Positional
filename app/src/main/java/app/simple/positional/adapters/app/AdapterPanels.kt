package app.simple.positional.adapters.app

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.model.BottomBar

class AdapterPanels(private val items: ArrayList<BottomBar>, private val onClick: (View, String, Int) -> Unit) : RecyclerView.Adapter<AdapterPanels.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_panel, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]

        holder.icon.setImageResource(item.icon)
        holder.icon.imageTintList = ColorStateList.valueOf(item.color)
        holder.label.text = item.name

        holder.container.setOnClickListener {
            onClick(it, item.tag, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }
}
