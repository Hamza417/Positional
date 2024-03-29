package app.simple.positional.adapters.miscellaneous

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.constants.LauncherBackground.vectorBackground
import app.simple.positional.constants.LauncherBackground.vectorBackgroundNight
import app.simple.positional.constants.LauncherBackground.vectorColors
import app.simple.positional.constants.LauncherBackground.vectorNightColors
import app.simple.positional.constants.LocationPins
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.BitmapHelper.addLinearGradient
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import com.google.android.material.card.MaterialCardView

class ArtsAdapter : RecyclerView.Adapter<ArtsAdapter.Holder>() {

    private val radius = MainPreferences.getCornerRadius()
    private val list = intArrayOf(*vectorBackground, *vectorBackgroundNight)
    private val colors = arrayOf(*vectorColors, *vectorNightColors)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_arts, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.art.setImageResource(list[position])
        holder.count.text = position.plus(1).toString() + "/" + list.size

        holder.icon.setImageBitmap(LocationPins.locationsPins[GPSPreferences.getPinSkin()]
                .toBitmapKeepingSize(holder.itemView.context, 6, 255)
                .addLinearGradient(intArrayOf(colors[position][0], colors[position][1])))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            holder.container.outlineAmbientShadowColor = colors[position][0]
            holder.container.outlineSpotShadowColor = colors[position][0]
        }
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
        val icon: ImageView = itemView.findViewById(R.id.arts_icon)
        val count: TextView = itemView.findViewById(R.id.arts_count)
        val container: MaterialCardView = itemView.findViewById(R.id.arts_container)

        init {
            container.radius = radius.toFloat()
        }
    }
}
