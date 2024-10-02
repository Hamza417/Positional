package app.simple.positional.adapters.settings

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerAccentColor
import app.simple.positional.decorations.ripple.Utils
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MainPreferences.getCornerRadius
import app.simple.positional.util.ColorUtils.toHex
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.HtmlHelper
import java.util.Arrays

class AccentColorAdapter(private val list: ArrayList<Pair<Int, String>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var palettesAdapterCallbacks: PalettesAdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            VerticalListViewHolder.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_accent_colors, parent, false))
            }
            VerticalListViewHolder.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_accent_color_header, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            holder.color.backgroundTintList = ColorStateList.valueOf(list[position].first)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                holder.color.outlineSpotShadowColor = list[position].first
                holder.color.outlineAmbientShadowColor = list[position].first
            }

            holder.container.setOnClickListener {
                palettesAdapterCallbacks.onColorPressed(list[position].first)

                if (list[position].second == "Material You (Dynamic)") {
                    MainPreferences.setMaterialYouAccentColor(true)
                } else {
                    MainPreferences.setMaterialYouAccentColor(false)
                }
            }

            holder.name.text = list[position].second
            holder.hex.text = list[position].first.toHex()

            holder.container.background = null
            holder.container.background = getRippleDrawable(holder.container.background, list[position].first)

            holder.tick.visibility = if (MainPreferences.isMaterialYouAccentColor()) {
                if (list[position].second == "Material You (Dynamic)") {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            } else {
                if (list[position].first == MainPreferences.getAccentColor()) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        } else if (holder is Header) {
            holder.total.text = HtmlHelper.fromHtml("<b>${holder.itemView.context.getString(R.string.total)}</b> ${list.size}")
        }
    }

    override fun getItemCount(): Int {
        return 23 + 1 // Extra 1 is Header
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            VerticalListViewHolder.TYPE_HEADER
        } else VerticalListViewHolder.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val color: DynamicCornerAccentColor = itemView.findViewById(R.id.adapter_palette_color)
        val tick: ImageView = itemView.findViewById(R.id.adapter_palette_tick)
        val name: TextView = itemView.findViewById(R.id.color_name)
        val hex: TextView = itemView.findViewById(R.id.color_hex)
        val container: LinearLayout = itemView.findViewById(R.id.color_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.adapter_accent_total)
    }

    fun setOnPaletteChangeListener(palettesAdapterCallbacks: PalettesAdapterCallbacks) {
        this.palettesAdapterCallbacks = palettesAdapterCallbacks
    }

    private fun getRippleDrawable(backgroundDrawable: Drawable?, color: Int): RippleDrawable {
        val outerRadii = FloatArray(8)
        val innerRadii = FloatArray(8)
        Arrays.fill(outerRadii, getCornerRadius().toFloat())
        Arrays.fill(innerRadii, getCornerRadius().toFloat())
        val shape = RoundRectShape(outerRadii, null, innerRadii)
        val mask = ShapeDrawable(shape)
        val stateList = ColorStateList.valueOf(color)
        val rippleDrawable = RippleDrawable(stateList, backgroundDrawable, mask)
        rippleDrawable.alpha = Utils.alpha
        return rippleDrawable
    }

    companion object {
        interface PalettesAdapterCallbacks {
            fun onColorPressed(source: Int)
        }
    }
}
