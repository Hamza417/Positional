package app.simple.positional.adapters.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarModel
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout

class AdapterPanels(private val items: ArrayList<BottomBarModel>, private val onClick: (View, String, Int) -> Unit) : RecyclerView.Adapter<AdapterPanels.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_panel, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]

        holder.icon.setImageResource(item.icon)
        holder.label.text = item.name

        holder.container.setOnClickListener {
            onClick(it, item.name, position)
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