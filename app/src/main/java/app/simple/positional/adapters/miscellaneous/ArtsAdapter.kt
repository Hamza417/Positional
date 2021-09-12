package app.simple.positional.adapters.miscellaneous

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.constants.LauncherBackground.vectorBackground
import app.simple.positional.constants.LauncherBackground.vectorBackgroundNight
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.glide.utils.ArtLoader.loadArtDrawable
import app.simple.positional.preferences.MainPreferences
import com.google.android.material.card.MaterialCardView

class ArtsAdapter : RecyclerView.Adapter<ArtsAdapter.Holder>() {

    private val radius = MainPreferences.getCornerRadius()
    private val list = intArrayOf(*vectorBackground, *vectorBackgroundNight)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_arts, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.art.loadArtDrawable(list[holder.absoluteAdapterPosition])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val art: ImageView = itemView.findViewById(R.id.art)
        val container: MaterialCardView = itemView.findViewById(R.id.arts_container)

        init {
            container.radius = radius.toFloat()
        }
    }
}
