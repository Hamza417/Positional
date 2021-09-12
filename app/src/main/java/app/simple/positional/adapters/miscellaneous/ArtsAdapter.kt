package app.simple.positional.adapters.miscellaneous

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.constants.LauncherBackground.vectorBackground
import app.simple.positional.constants.LauncherBackground.vectorBackgroundNight
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.preferences.MainPreferences
import com.google.android.material.card.MaterialCardView

class ArtsAdapter : RecyclerView.Adapter<ArtsAdapter.Holder>() {

    private val radius = MainPreferences.getCornerRadius()
    private val list = intArrayOf(*vectorBackground, *vectorBackgroundNight)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_arts, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.art.setImageDrawable(
                ContextCompat.getDrawable(holder.itemView.context, list[position]))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val art: ImageView = itemView.findViewById(R.id.art)
        val container: MaterialCardView = itemView.findViewById(R.id.arts_container)

        init {
            container.radius = radius.toFloat()
        }
    }
}
