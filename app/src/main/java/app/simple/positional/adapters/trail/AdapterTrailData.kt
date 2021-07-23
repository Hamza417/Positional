package app.simple.positional.adapters.trail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.R.id
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.TrailData
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.TimeFormatter.formatDate

class AdapterTrailData(private val trailData: ArrayList<TrailData>) : RecyclerView.Adapter<AdapterTrailData.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trail_data, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.setImageResource(TrailIcons.icons[trailData[position].iconPosition])
        holder.name.text = trailData[position].name ?: "--"
        holder.note.text = trailData[position].note ?: holder.itemView.context.getString(R.string.not_available)
        holder.date.text = trailData[position].timeAdded.formatDate()
        holder.coordinates.text = with(holder.itemView.context) {
            getString(R.string.coordinates_format,
                      DMSConverter.latitudeAsDMS(trailData[position].latitude, 2, this),
                      DMSConverter.longitudeAsDMS(trailData[position].longitude, 2, this))
        }
    }

    override fun getItemCount(): Int {
        return trailData.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(id.adapter_trail_data_icon)
        val name: TextView = itemView.findViewById(id.adapter_trail_data_name)
        val note: TextView = itemView.findViewById(id.adapter_trail_data_notes)
        val coordinates: TextView = itemView.findViewById(id.adapter_trail_data_coordinates)
        val date: TextView = itemView.findViewById(id.adapter_trail_data_date)
    }
}