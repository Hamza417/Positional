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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.fastscroll.PopupTextProvider
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.LocaleHelper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TimeZoneAdapter(var timeZones: MutableList<Pair<String, String>>, var searchText: String) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        PopupTextProvider {

    lateinit var context: Context
    private var currentSelectedTimezone = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        timeZones.sortBy { it.first }
        context = parent.context
        currentSelectedTimezone = ClockPreferences.getTimeZone()
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_timezone, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Holder) {

            holder.timeZone.text = timeZones[position].first
            holder.offset.text = timeZones[position].second

            searchTimeZones(holder.timeZone, timeZones[position].first)
            searchTimeZones(holder.offset, timeZones[position].second)

            holder.timeZone.setTextColor(if (timeZones[position].first == currentSelectedTimezone) {
                holder.indicator.visibility = View.VISIBLE
                holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent)
            } else {
                holder.indicator.visibility = View.INVISIBLE
                ContextCompat.getColor(holder.itemView.context, R.color.textPrimary)
            })

            holder.format.text = formattedTime(timeZones[position].first)

            holder.layout.setOnClickListener {
                ClockPreferences.setTimezoneSelectedPosition(position)
                ClockPreferences.setTimeZone(timeZones[position].first)
                currentSelectedTimezone = timeZones[position].first
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return timeZones.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val timeZone: TextView = itemView.findViewById(R.id.time_zone_adapter_text)
        val offset: TextView = itemView.findViewById(R.id.time_zone_adapter_offset)
        val indicator: ImageView = itemView.findViewById(R.id.time_zone_indicator)
        val format: TextView = itemView.findViewById(R.id.time_zone_adapter_format)
        val layout: DynamicRippleConstraintLayout = itemView.findViewById(R.id.time_zone_adapter_item_container)
    }

    private fun formattedTime(timezone: String): String {
        return Instant.now().atZone(ZoneId.of(timezone))
                .format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    private fun searchTimeZones(textView: TextView, string: String) {
        if (string == "") return
        val sb = SpannableStringBuilder(string)
        val startPos =
            string.lowercase(Locale.getDefault()).indexOf(searchText.lowercase(Locale.getDefault()))
        val endPos = startPos + searchText.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(ContextCompat.getColor(context, R.color.iconRegular)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            sb.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }

        textView.text = sb
    }

    override fun getPopupText(position: Int): String {
        return timeZones[position].first.substring(0, 1).uppercase(LocaleHelper.getAppLocale())
    }
}
