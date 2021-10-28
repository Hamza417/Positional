package app.simple.positional.adapters.settings

import android.annotation.SuppressLint
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
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.ViewUtils
import com.google.android.material.card.MaterialCardView

class LocationsAdapter : RecyclerView.Adapter<LocationsAdapter.Holder>() {

    private var locations: MutableList<Locations> = arrayListOf()
    private var locationsAdapterCallback: LocationsCallback? = null

    private val radius = MainPreferences.getCornerRadius()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_locations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("Locations:", "${locations[position].latitude} : ${locations[position].longitude}")
        holder.address.text = locations[position].address
        holder.latitude.text = DMSConverter.latitudeAsDMS(locations[position].latitude, holder.itemView.context)
        holder.longitude.text = DMSConverter.longitudeAsDMS(locations[position].longitude, holder.itemView.context)

        holder.container.setOnClickListener {
            locationsAdapterCallback?.onLocationClicked(locations = locations[position])
        }

        holder.container.setOnLongClickListener {
            locationsAdapterCallback?.onLocationLongClicked(locations = locations[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(locations: MutableList<Locations>) {
        this.locations = locations
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addLocation(locations: Locations) {
        this.locations.add(0, locations)
        notifyItemInserted(0)
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
        val container: MaterialCardView = itemView.findViewById(R.id.adapter_locations_container)
        val address: TextView = itemView.findViewById(R.id.adapter_locations_address)
        val latitude: TextView = itemView.findViewById(R.id.adapter_locations_latitude)
        val longitude: TextView = itemView.findViewById(R.id.adapter_locations_longitude)

        init {
            container.radius = radius.toFloat()
            ViewUtils.addShadow(container)
        }
    }

    interface LocationsCallback {
        fun onLocationClicked(locations: Locations)
        fun onLocationLongClicked(locations: Locations)
    }
}
