package app.simple.positional.adapters.trail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.viewholders.VerticalListViewHolder

class AdapterPopupMarkers : RecyclerView.Adapter<AdapterPopupMarkers.Holder>() {

    var onIconClicked: (position: Int) -> Unit = {}
    var onIconLongClicked: (position: Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.adapter_popup_markers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.setImageResource(TrailIcons.icons[position])

        holder.icon.setOnClickListener {
            onIconClicked.invoke(position)
        }

        holder.icon.setOnLongClickListener {
            onIconLongClicked.invoke(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return TrailIcons.icons.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: DynamicRippleImageButton = itemView.findViewById(R.id.marker)
    }
}