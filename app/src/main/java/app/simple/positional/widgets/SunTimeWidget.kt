package app.simple.positional.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.SunTimes
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class SunTimeWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        onWidgetUpdate(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        onWidgetUpdate(context)
        super.onEnabled(context)
    }

    private fun onWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {

            val latitude = SharedPreferences.getSharedPreferences(context).getFloat(GPSPreferences.lastLatitude, 0.0F).toDouble()
            val longitude = SharedPreferences.getSharedPreferences(context).getFloat(GPSPreferences.lastLongitude, 0.0F).toDouble()

            val pattern: DateTimeFormatter = if (ClockPreferences.getDefaultClockTime()) {
                DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(LocaleHelper.getAppLocale())
            } else {
                DateTimeFormatter.ofPattern("HH:mm:ss")
            }

            val sunTimes = SunTimes.compute().timezone(Calendar.getInstance().timeZone.id).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
            val sunTimeData = HtmlHelper.fromHtml("<b>${context.getString(R.string.sun_sunrise)}</b> ${pattern.format(sunTimes.rise)}<br>" +
                    "<b>${context.getString(R.string.sun_sunset)}</b> ${pattern.format(sunTimes.set)}<br>" +
                    "<b>${context.getString(R.string.sun_noon)}</b> ${pattern.format(sunTimes.noon)}<br>" +
                    "<b>${context.getString(R.string.sun_nadir)}</b> ${pattern.format(sunTimes.nadir)}")

            withContext(Dispatchers.Main) {
                val views = RemoteViews(context.packageName, R.layout.widget_suntime)
                views.setTextViewText(R.id.sun_time_data_widget, sunTimeData)

                val componentName = ComponentName(context, SunTimeWidget::class.java)
                val manager = AppWidgetManager.getInstance(context)
                manager.updateAppWidget(componentName, views)
            }
        }
    }
}