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
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible

class BottomBarAdapter(private var list: ArrayList<BottomBarModel>) : RecyclerView.Adapter<BottomBarAdapter.Holder>() {

    private var currentTag = FragmentPreferences.getCurrentTag()
    private var lastItem = FragmentPreferences.getCurrentPage()
    private var bottomBarCallbacks: BottomBarCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_bar, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (currentTag == list[position].tag) {
            holder.bg.visible(animate = true)

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconColor))
        } else {
            holder.bg.invisible(animate = true)

            holder.icon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.iconRegular))
        }

        holder.icon.setImageResource(list[position].icon)

        holder.container.setOnClickListener {
            currentTag = list[position].tag
            notifyItemChanged(lastItem)
            FragmentPreferences.setCurrentPage(position)
            FragmentPreferences.setCurrentTag(list[position].tag)
            notifyItemChanged(position)
            lastItem = position
            bottomBarCallbacks?.onItemClicked(position, list[position].tag)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)
        holder.bg.clearAnimation()
    }

    fun setBottomBarItems(list: ArrayList<BottomBarModel>) {
        this.list = list

        /**
         * Since this panels can only be edited from the settings panel
         * it is safe to assume that last item is list's total size minus
         * one. This will fix the bug of bottom bar settings icon stays selected
         * issue
         */
        lastItem = list.size - 1

        /**
         * Now refresh the bottom panel
         */
        notifyDataSetChanged()
    }

    fun setOnBottomBarCallbackListener(bottomBarCallbacks: BottomBarCallbacks) {
        this.bottomBarCallbacks = bottomBarCallbacks
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.bottom_bar_item)
        val bg: DynamicCornerFrameLayout = itemView.findViewById(R.id.bottom_bar_item_background)
        val container: FrameLayout = itemView.findViewById(R.id.bottom_bar_container)
    }
}