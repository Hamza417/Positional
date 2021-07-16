package app.simple.positional.adapters.bottombar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.preferences.BottomBarPreferences

class AdapterPanelEditor(private val items: ArrayList<BottomBarModel>) : RecyclerView.Adapter<AdapterPanelEditor.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_panel_editor, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = items[position].name

        holder.name.setCompoundDrawablesWithIntrinsicBounds(
                items[position].icon,
                0,
                0,
                0)

        holder.switch.isChecked = when (items[position].tag) {
            "clock" -> {
                BottomBarPreferences.getClockPanelVisibility()
            }
            "compass" -> {
                BottomBarPreferences.getCompassPanelVisibility()
            }
            "location" -> {
                BottomBarPreferences.getGpsPanelVisibility()
            }
            "trail" -> {
                BottomBarPreferences.getTrailPanelVisibility()
            }
            "level" -> {
                BottomBarPreferences.getLevelPanelVisibility()
            }
            "settings" -> {
                holder.switch.isClickable = false
                BottomBarPreferences.getSettingsPanelVisibility()
            }
            else -> false
        }

        holder.switch.setOnCheckedChangeListener {
            when (items[position].tag) {
                "clock" -> {
                    BottomBarPreferences.setClockPanelVisibility(it)
                }
                "compass" -> {
                    BottomBarPreferences.setCompassPanelVisibility(it)
                }
                "location" -> {
                    BottomBarPreferences.setGpsPanelVisibility(it)
                }
                "trail" -> {
                    BottomBarPreferences.setTrailPanelVisibility(it)
                }
                "level" -> {
                    BottomBarPreferences.setLevelPanelVisibility(it)
                }
                "settings" -> {
                    BottomBarPreferences.setSettingsPanelVisibility(it)
                }
            }
        }

        if (items[position].tag == "settings") {
            holder.switch.isClickable = false
            holder.switch.isEnabled = false
            holder.container.alpha = 0.4F
            holder.container.isClickable = false
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    open inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_panel_editor_container)
        val name: TextView = itemView.findViewById(R.id.adapter_panel_editor_name)
        val switch: SwitchView = itemView.findViewById(R.id.adapter_panel_editor_switch)
    }
}