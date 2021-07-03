package app.simple.positional.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.viewholders.VerticalListViewHolder

class AdapterTrailIcons : RecyclerView.Adapter<AdapterTrailIcons.Holder>() {

    var onIconClicked: (position: Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.adpater_trail_icons, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.setImageResource(TrailIcons.icons[position])

        holder.icon.setOnClickListener {
            onIconClicked.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return TrailIcons.icons.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_trail_icon)
    }
}