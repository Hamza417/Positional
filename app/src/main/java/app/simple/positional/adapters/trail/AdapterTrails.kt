package app.simple.positional.adapters.trail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.TrailModel
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ColorUtils.resolveAttrColor
import java.text.DateFormat
import java.util.*

class AdapterTrails(private val list: ArrayList<TrailModel>) : RecyclerView.Adapter<AdapterTrails.Holder>() {

    var onTrailNameClicked: (name: String) -> Unit = {}
    var currentTrail = TrailPreferences.getLastUsedTrail()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trails, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].trailName
        holder.date.text = list[position].dateCreated.formatDate()

        if (list[position].trailName == currentTrail) {
            holder.name.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))
        } else {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textPrimary))
        }

        holder.container.setOnClickListener {
            currentTrail = list[position].trailName
            TrailPreferences.setLastTrailName(list[position].trailName)
            notifyDataSetChanged()
            onTrailNameClicked.invoke(list[position].trailName)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun Long.formatDate(): String {
        return DateFormat.getDateTimeInstance().format(Date(this))
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.trails_name)
        val date: TextView = itemView.findViewById(R.id.trails_date)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_trails_container)
    }
}