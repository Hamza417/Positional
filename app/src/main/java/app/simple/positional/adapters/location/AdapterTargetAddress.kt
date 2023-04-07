package app.simple.positional.adapters.location

import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.util.DMSConverter

class AdapterTargetAddress(private val addresses: MutableList<Address>) : RecyclerView.Adapter<AdapterTargetAddress.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_address, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(holder) {
            address.text = addresses[position].getAddressLine(0)
            latitude.text = DMSConverter.getFormattedLatitude(addresses[position].latitude, itemView.context)
            longitude.text = DMSConverter.getFormattedLongitude(addresses[position].longitude, itemView.context)
        }
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_address_container)
        val address: TextView = itemView.findViewById(R.id.adapter_address_address)
        val latitude: TextView = itemView.findViewById(R.id.adapter_address_latitude)
        val longitude: TextView = itemView.findViewById(R.id.adapter_address_longitude)
    }
}