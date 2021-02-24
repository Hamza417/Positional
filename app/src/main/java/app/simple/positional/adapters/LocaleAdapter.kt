package app.simple.positional.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.callbacks.LocaleCallback
import app.simple.positional.model.Locales
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.LocaleHelper
import java.util.*

class LocaleAdapter : RecyclerView.Adapter<LocaleAdapter.Holder>() {

    var localeCallback: LocaleCallback? = null
    private var localeList: MutableList<Locales> = LocaleHelper.localeList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        localeList.sortBy { it.language.toLowerCase(Locale.ROOT) }
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_locales, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.locale.text = if (position == 0) holder.itemView.context.getString(R.string.auto_system_default_language) else localeList[position].language
        holder.indicator.isVisible = localeList[position].localeCode == MainPreferences.getAppLanguage()

        holder.container.isClickable = localeList[position].localeCode != MainPreferences.getAppLanguage()
        holder.container.isEnabled = localeList[position].localeCode != MainPreferences.getAppLanguage()

        holder.container.setOnClickListener {
            localeCallback?.onLocaleSet(localeList[position].localeCode)
        }
    }

    override fun getItemCount(): Int {
        return localeList.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val indicator: ImageView = itemView.findViewById(R.id.locale_indicator)
        val locale: TextView = itemView.findViewById(R.id.locales_adapter_text)
        val container: LinearLayout = itemView.findViewById(R.id.locales_adapter_item_container)
    }
}
