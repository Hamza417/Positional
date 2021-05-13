package app.simple.positional.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.callbacks.LocationAdapterCallback
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.model.Locations
import app.simple.positional.util.DMSConverter.latitudeAsDMS
import app.simple.positional.util.DMSConverter.longitudeAsDMS
import app.simple.positional.util.bouncyValue
import app.simple.positional.util.stiffnessValue

class LocationsAdapter : RecyclerView.Adapter<LocationsAdapter.Holder>() {

    private var locations: MutableList<Locations> = arrayListOf()
    var locationsAdapterCallback: LocationAdapterCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_locations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.address.text = locations[position].address
        holder.latitude.text = latitudeAsDMS(locations[position].latitude, 2, holder.itemView.context)
        holder.longitude.text = longitudeAsDMS(locations[position].longitude, 2, holder.itemView.context)

        holder.container.setOnClickListener {
            locationsAdapterCallback?.onLocationItemClicked(locations = locations[position])
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_locations_container)
        val address: TextView = itemView.findViewById(R.id.adapter_locations_address)
        val latitude: TextView = itemView.findViewById(R.id.adapter_locations_latitude)
        val longitude: TextView = itemView.findViewById(R.id.adapter_locations_longitude)

        private var currentVelocity = 0f

        val rotation: SpringAnimation = SpringAnimation(itemView, SpringAnimation.ROTATION)
                .setSpring(
                        SpringForce()
                                .setFinalPosition(0f)
                                .setDampingRatio(bouncyValue)
                                .setStiffness(stiffnessValue)
                )
                .addUpdateListener { _, _, velocity ->
                    currentVelocity = velocity
                }

        val translationY: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
                .setSpring(
                        SpringForce()
                                .setFinalPosition(0f)
                                .setDampingRatio(bouncyValue)
                                .setStiffness(stiffnessValue)
                )
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
}
