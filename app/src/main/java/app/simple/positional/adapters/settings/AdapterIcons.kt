package app.simple.positional.adapters.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.activities.alias.*
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible

class AdapterIcons : RecyclerView.Adapter<AdapterIcons.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_icons, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.setImageResource(list[position])

        if (position == getIconStatus(holder.itemView.context)) {
            holder.tick.visible(false)
        } else {
            holder.tick.invisible(false)
        }

        holder.container.setOnClickListener {
            setIcon(holder.itemView.context, position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_icon_iv)
        val tick: ImageView = itemView.findViewById(R.id.adapter_icon_tick)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_icon_container)
    }

    private fun getIconStatus(context: Context): Int {
        return when (PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconOneAlias::class.java)) -> {
                0
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconLegacyAlias::class.java)) -> {
                1
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconTwoAlias::class.java)) -> {
                2
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconThreeAlias::class.java)) -> {
                3
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconFourAlias::class.java)) -> {
                4
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconFiveAlias::class.java)) -> {
                5
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconSixAlias::class.java)) -> {
                6
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconSevenAlias::class.java)) -> {
                7
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconEightAlias::class.java)) -> {
                8
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconNineAlias::class.java)) -> {
                9
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconTenAlias::class.java)) -> {
                10
            }
            context.packageManager.getComponentEnabledSetting(ComponentName(context, IconElevenAlias::class.java)) -> {
                11
            }
            else -> {
                0
            }
        }
    }

    private fun setIcon(context: Context, position: Int) {
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconOneAlias::class.java), getStatusFromPosition(position == 0), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconLegacyAlias::class.java), getStatusFromPosition(position == 1), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconTwoAlias::class.java), getStatusFromPosition(position == 2), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconThreeAlias::class.java), getStatusFromPosition(position == 3), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconFourAlias::class.java), getStatusFromPosition(position == 4), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconFiveAlias::class.java), getStatusFromPosition(position == 5), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconSixAlias::class.java), getStatusFromPosition(position == 6), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconSevenAlias::class.java), getStatusFromPosition(position == 7), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconEightAlias::class.java), getStatusFromPosition(position == 8), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconNineAlias::class.java), getStatusFromPosition(position == 9), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconTenAlias::class.java), getStatusFromPosition(position == 10), PackageManager.DONT_KILL_APP)
        context.packageManager.setComponentEnabledSetting(ComponentName(context, IconElevenAlias::class.java), getStatusFromPosition(position == 11), PackageManager.DONT_KILL_APP)
    }

    private fun getStatusFromPosition(position: Boolean): Int {
        return if (position) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
    }

    companion object {
        val list = arrayListOf(
                R.mipmap.ic_launcher_default,
                R.mipmap.ic_launcher_legacy,
                R.mipmap.ic_launcher_two,
                R.mipmap.ic_launcher_three,
                R.mipmap.ic_launcher_four,
                R.mipmap.ic_launcher_five,
                R.mipmap.ic_launcher_six,
                R.mipmap.ic_launcher_seven,
                R.mipmap.ic_launcher_eight,
                R.mipmap.ic_launcher_nine,
                R.mipmap.ic_launcher_ten,
                R.mipmap.ic_launcher_eleven
        )
    }
}