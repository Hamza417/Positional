package app.simple.positional.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.callbacks.TimeZonesCallback
import app.simple.positional.util.bouncyValue
import app.simple.positional.util.resolveAttrColor
import app.simple.positional.util.stiffnessValue
import java.util.*

class TimeZoneAdapter(
        var timeZones: MutableList<String>,
        private val timeZonesCallback: TimeZonesCallback,
        var searchText: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        timeZones.sort()
        context = parent.context
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_timezone, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Holder) {
            holder.timeZone.text = timeZones[position]
            searchTimeZones(holder.timeZone, timeZones[position])
            timeZonesCallback.itemSubstring(timeZones[position].substring(0, 2))
            holder.layout.setOnClickListener {
                timeZonesCallback.itemClicked(timeZones[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return timeZones.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeZone: TextView = itemView.findViewById(R.id.time_zone_adapter_text)
        val layout: LinearLayout = itemView.findViewById(R.id.time_zone_adapter_item_container)

        var currentVelocity = 0f

        val rotation: SpringAnimation = SpringAnimation(itemView, SpringAnimation.ROTATION)
                .setSpring(
                        SpringForce()
                                .setFinalPosition(0f)
                                .setDampingRatio(bouncyValue)
                                .setStiffness(stiffnessValue)
                )
                .addUpdateListener { _, _, velocity ->
                    currentVelocity = velocity
                }

        val translationY: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
                .setSpring(
                        SpringForce()
                                .setFinalPosition(0f)
                                .setDampingRatio(bouncyValue)
                                .setStiffness(stiffnessValue)
                )
    }

    private fun searchTimeZones(textView: TextView, string: String) {
        if (string == "") return
        val sb = SpannableStringBuilder(string)
        val startPos = string.toLowerCase(Locale.getDefault()).indexOf(searchText.toLowerCase(Locale.getDefault()))
        val endPos = startPos + searchText.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(context.resolveAttrColor(R.attr.switcher_on_start_color)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            sb.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        textView.text = sb
    }
}