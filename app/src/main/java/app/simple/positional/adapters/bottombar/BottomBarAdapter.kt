package app.simple.positional.adapters.bottombar

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerFrameLayout
import app.simple.positional.preferences.FragmentPreferences
import app.simple.positional.util.ViewUtils.makeInvisible
import app.simple.positional.util.ViewUtils.makeVisible

class BottomBarAdapter(private val list: ArrayList<BottomBarModel>) : RecyclerView.Adapter<BottomBarAdapter.Holder>() {

    private var lastItem = FragmentPreferences.getCurrentPage()
    var onItemClicked:
            (position: Int, name: String) -> Unit = { _: Int, _: String -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_bar, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bg.clearAnimation()

        if (position == lastItem) {
            holder.bg.makeVisible(animate = true)

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconColor))
        } else {
            holder.bg.makeInvisible(animate = true)

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconRegular))
        }

        holder.icon.setImageResource(list[position].icon)

        holder.container.setOnClickListener {
            notifyItemChanged(lastItem)
            notifyItemChanged(position)
            lastItem = position
            onItemClicked.invoke(position, list[position].name)
            FragmentPreferences.setCurrentPage(position)
            FragmentPreferences.setCurrentTag(list[position].name)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)
        holder.bg.clearAnimation()
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.bottom_bar_item)
        val bg: DynamicCornerFrameLayout = itemView.findViewById(R.id.bottom_bar_item_background)
        val container: FrameLayout = itemView.findViewById(R.id.bottom_bar_container)
    }
}