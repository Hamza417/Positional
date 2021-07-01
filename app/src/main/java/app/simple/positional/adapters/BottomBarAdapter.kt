package app.simple.positional.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.bottombar.BottomBarModel
import app.simple.positional.preferences.FragmentPreferences

class BottomBarAdapter(private val list: ArrayList<BottomBarModel>) : RecyclerView.Adapter<BottomBarAdapter.Holder>() {

    private var lastItem = 0
    var onItemClicked:
            (position: Int, name: String) -> Unit = { _: Int, _: String -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        lastItem = FragmentPreferences.getCurrentPage()
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_bar, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (position == lastItem) {
            holder.bg.animate()
                .scaleX(1F)
                .scaleY(1F)
                .alpha(1F)
                .setInterpolator(DecelerateInterpolator(1.5F))
                .setDuration(holder.itemView.context.resources.getInteger(R.integer.animation_duration).toLong())
                .start()

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconColor))
        } else {
            holder.bg.animate()
                .scaleX(0F)
                .scaleY(0F)
                .alpha(0F)
                .setInterpolator(DecelerateInterpolator(1.5F))
                .setDuration(holder.itemView.context.resources.getInteger(R.integer.animation_duration).toLong())
                .start()

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconRegular))
        }

        holder.icon.setImageResource(list[position].icon)

        holder.container.setOnClickListener {
            notifyItemChanged(lastItem)
            lastItem = position
            onItemClicked.invoke(position, list[position].name)
            FragmentPreferences.setCurrentPage(position)
            FragmentPreferences.setCurrentTag(list[position].name)
            notifyItemChanged(lastItem)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icon: ImageView = itemView.findViewById(R.id.bottom_bar_item)
        val bg: ImageView = itemView.findViewById(R.id.bottom_bar_item_background)
        val container: FrameLayout = itemView.findViewById(R.id.bottom_bar_container)
    }
}