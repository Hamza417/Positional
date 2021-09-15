package app.simple.positional.adapters.mainscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.model.MainListModel
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ViewUtils
import com.google.android.material.card.MaterialCardView

class MainScreenAdapter(private val list: ArrayList<MainListModel>) : RecyclerView.Adapter<MainScreenAdapter.Holder>() {

    private val corner = MainPreferences.getCornerRadius()

    var onMainItemClicked: ((icon: View, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_main_list, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.setImageResource(list[holder.absoluteAdapterPosition].icon)
        holder.label.text = list[holder.absoluteAdapterPosition].name

        holder.icon.transitionName = "panel_icon_$position"

        holder.container.setOnClickListener {
            onMainItemClicked?.invoke(holder.icon, holder.absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.main_list_icon)
        val label: TextView = itemView.findViewById(R.id.main_list_label)
        val container: MaterialCardView = itemView.findViewById(R.id.adapter_main_list_item_container)

        init {
            container.radius = corner.toFloat()
            ViewUtils.addShadow(container)
        }
    }
}