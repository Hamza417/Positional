package app.simple.positional.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.ui.panels.Time
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.SunTimes
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TwilightWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        context?.updateWidget()
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.updateWidget()
    }

    private fun Context.updateWidget() {
        CoroutineScope(Dispatchers.Default).launch {
            SharedPreferences.init(this@updateWidget)

            val latitude = if (MainPreferences.isCustomCoordinate()) {
                SharedPreferences.getSharedPreferences().getFloat(MainPreferences.latitude, 0.0F).toDouble()
            } else {
                SharedPreferences.getSharedPreferences().getFloat(MainPreferences.lastLatitude, 0.0F).toDouble()
            }

            val longitude = if (MainPreferences.isCustomCoordinate()) {
                SharedPreferences.getSharedPreferences().getFloat(MainPreferences.longitude, 0.0F).toDouble()
            } else {
                SharedPreferences.getSharedPreferences().getFloat(MainPreferences.lastLongitude, 0.0F).toDouble()
            }

            val pattern: DateTimeFormatter = if (ClockPreferences.getDefaultClockTimeFormat()) {
                DateTimeFormatter.ofPattern("hh:mm a").withLocale(LocaleHelper.getAppLocale())
            } else {
                DateTimeFormatter.ofPattern("HH:mm")
            }

            val updateTime = getString(R.string.last_updated) + " • " + pattern.format(ZonedDateTime.now())

            val twilightData =
                with(SunTimes.compute().timezone(ClockPreferences.getTimeZone()).on(Instant.now()).latitude(latitude).longitude(longitude)) {
                    HtmlHelper.fromHtml(
                        "<h3><b>${getString(R.string.twilight_morning)}</b></h3>" +
                        "<b><font color='#1a237e'>$DOT</font> ${getString(R.string.twilight_astronomical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().rise)}<br>" +
                        "<b><font color='#0d47a1'>$DOT</font> ${getString(R.string.twilight_nautical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().rise)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().rise)}<br>" +
                        "<b><font color='#1565c0'>$DOT</font> ${getString(R.string.twilight_blue_hour)}</b> ${pattern.format(twilight(SunTimes.Twilight.NIGHT_HOUR).execute().rise)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.BLUE_HOUR).execute().rise)}<br>" +
                        "<b><font color='#1e88e5'>$DOT</font> ${getString(R.string.twilight_civil_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().rise)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.HORIZON).execute().rise)}<br>" +
                        "<b><font color='#42a5f5'>$DOT</font> ${getString(R.string.twilight_visual)}</b> ${pattern.format(twilight(SunTimes.Twilight.VISUAL).execute().rise)}<br>" +
                        "<b><font color='#64b5f6'>$DOT</font> ${getString(R.string.twilight_horizon)}</b> ${pattern.format(twilight(SunTimes.Twilight.HORIZON).execute().rise)}<br>" +
                        "<b><font color='#90caf9'>$DOT</font> ${getString(R.string.twilight_visual_lower)}</b> ${pattern.format(twilight(SunTimes.Twilight.VISUAL_LOWER).execute().rise)}<br>" +
                        "<b><font color='#ffcc80'>$DOT</font> ${getString(R.string.twilight_golden_hour)}</b> ${pattern.format(twilight(Time.GOLDEN_HOUR_MORNING_START).execute().rise)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.GOLDEN_HOUR).execute().rise)}<br>" +
                        "<br>" +
                        "<h3>${getString(R.string.twilight_evening)}</h3>" +
                        "<b><font color='#ffcc80'>$DOT</font> ${getString(R.string.twilight_golden_hour)}</b> ${pattern.format(twilight(SunTimes.Twilight.GOLDEN_HOUR).execute().set)} ${Time.Companion.ARROW} ${pattern.format(twilight(Time.Companion.GOLDEN_HOUR_EVENING_END).execute().set)}<br>" +
                        "<b><font color='#90caf9'>$DOT</font> ${getString(R.string.twilight_visual_lower)}</b> ${pattern.format(twilight(SunTimes.Twilight.VISUAL_LOWER).execute().set)}<br>" +
                        "<b><font color='#64b5f6'>$DOT</font> ${getString(R.string.twilight_horizon)}</b> ${pattern.format(twilight(SunTimes.Twilight.HORIZON).execute().set)}<br>" +
                        "<b><font color='#42a5f5'>$DOT</font> ${getString(R.string.twilight_visual)}</b> ${pattern.format(twilight(SunTimes.Twilight.VISUAL).execute().set)}<br>" +
                        "<b><font color='#1e88e5'>$DOT</font> ${getString(R.string.twilight_civil_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.HORIZON).execute().set)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().set)}<br>" +
                        "<b><font color='#1565c0'>$DOT</font> ${getString(R.string.twilight_blue_hour)}</b> ${pattern.format(twilight(SunTimes.Twilight.BLUE_HOUR).execute().set)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.NIGHT_HOUR).execute().set)}<br>" +
                        "<b><font color='#0d47a1'>$DOT</font> ${getString(R.string.twilight_nautical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().set)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().set)}<br>" +
                        "<b><font color='#1a237e'>$DOT</font> ${getString(R.string.twilight_astronomical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().set)} ${Time.Companion.ARROW} ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set)}<br>"
                    )
                }

            withContext(Dispatchers.Main) {
                val views = RemoteViews(packageName, R.layout.widget_twilight)
                views.setTextViewText(R.id.twilight_info, twilightData)
                views.setTextViewText(R.id.widget_update_time, updateTime)

                val componentName = ComponentName(this@updateWidget, TwilightWidget::class.java)
                val manager = AppWidgetManager.getInstance(this@updateWidget)
                manager.updateAppWidget(componentName, views)
            }
        }
    }

    companion object {
        private const val TAG = "TwilightWidget"
        private const val DOT = "⬤"
    }
}