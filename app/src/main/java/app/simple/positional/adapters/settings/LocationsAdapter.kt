package app.simple.positional.adapters.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.Locations
import app.simple.positional.util.DMSConverter

class LocationsAdapter : RecyclerView.Adapter<LocationsAdapter.Holder>() {

    private var locations: MutableList<Locations> = arrayListOf()
    var locationsAdapterCallback: LocationsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_locations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("Locations:", "${locations[position].latitude} : ${locations[position].longitude}")
        holder.address.text = locations[position].address
        holder.latitude.text = DMSConverter.latitudeAsDMS(locations[position].latitude, 3, holder.itemView.context)
        holder.longitude.text = DMSConverter.longitudeAsDMS(locations[position].longitude, 3, holder.itemView.context)

        holder.container.setOnClickListener {
            locationsAdapterCallback?.onLocationClicked(locations = locations[position])
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    fun setList(locations: MutableList<Locations>) {
        this.locations = locations
        notifyDataSetChanged()
    }

    fun clearList() {
        for (i in locations.indices) {
            removeItem(0)
        }
    }

    fun removeItem(position: Int): Locations {
        val p0 = locations[position]
        locations.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, locations.size)
        return p0
    }

    fun setOnLocationsCallbackListener(locationsCallback: LocationsCallback) {
        this.locationsAdapterCallback = locationsCallback
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_locations_container)
        val address: TextView = itemView.findViewById(R.id.adapter_locations_address)
        val latitude: TextView = itemView.findViewById(R.id.adapter_locations_latitude)
        val longitude: TextView = itemView.findViewById(R.id.adapter_locations_longitude)
    }

    interface LocationsCallback {
        fun onLocationClicked(locations: Locations)
    }
}
