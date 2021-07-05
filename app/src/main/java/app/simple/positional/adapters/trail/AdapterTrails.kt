package app.simple.positional.adapters.trail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class AdapterTrails(private val list: ArrayList<String>) : RecyclerView.Adapter<AdapterTrails.Holder>() {

    var onTrailNameClicked: (name: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trails, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position]

        holder.name.setOnClickListener {
            onTrailNameClicked.invoke(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: DynamicRippleTextView = itemView.findViewById(R.id.trails_name)
    }
}