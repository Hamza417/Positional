package app.simple.positional.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.constants.LauncherBackground
import app.simple.positional.constants.LocationPins
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.SunTimes
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SunTimeWidgetArt : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        onWidgetUpdate(context)

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        onWidgetUpdate(context)
    }

    private fun onWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {

            SharedPreferences.init(context)

            val latitude = if (MainPreferences.isCustomCoordinate()) {
                getSharedPreferences().getFloat(MainPreferences.latitude, 0.0F).toDouble()
            } else {
                getSharedPreferences().getFloat(GPSPreferences.lastLatitude, 0.0F).toDouble()
            }

            val longitude = if (MainPreferences.isCustomCoordinate()) {
                getSharedPreferences().getFloat(MainPreferences.longitude, 0.0F).toDouble()
            } else {
                getSharedPreferences().getFloat(GPSPreferences.lastLongitude, 0.0F).toDouble()
            }

            val pattern: DateTimeFormatter = if (ClockPreferences.getDefaultClockTime()) {
                DateTimeFormatter.ofPattern("hh:mm a").withLocale(LocaleHelper.getAppLocale())
            } else {
                DateTimeFormatter.ofPattern("HH:mm")
            }

            val sunTimes = SunTimes.compute().timezone(Calendar.getInstance().timeZone.id).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
            val sunRise = pattern.format(sunTimes.rise)
            val sunNoon = pattern.format(sunTimes.noon)
            val sunSet = pattern.format(sunTimes.set)
            val sunNadir = pattern.format(sunTimes.nadir)

            val updateTime = context.getString(R.string.last_updated) + " • " + pattern.format(ZonedDateTime.now())

            withContext(Dispatchers.Main) {
                val views = RemoteViews(context.packageName, R.layout.widget_suntime_art)
                views.setTextViewText(R.id.widget_sunrise, sunRise)
                views.setTextViewText(R.id.widget_sun_noon, sunNoon)
                views.setTextViewText(R.id.widget_sunset, sunSet)
                views.setTextViewText(R.id.widget_sun_nadir, sunNadir)

                views.setTextViewText(R.id.widget_suntime_heading, updateTime)

                views.setImageViewResource(R.id.widget_suntime_art,
                        LauncherBackground.vectorBackground[LauncherBackground.vectorBackground.indices.random()])

                if (MainPreferences.isCustomCoordinate()) {
                    views.setImageViewResource(R.id.widget_suntime_icon, R.drawable.ic_place_custom)
                } else {
                    views.setImageViewResource(R.id.widget_suntime_icon, LocationPins.locationsPins[GPSPreferences.getPinSkin()])
                }

                val componentName = ComponentName(context, SunTimeWidgetArt::class.java)
                val manager = AppWidgetManager.getInstance(context)
                manager.updateAppWidget(componentName, views)
            }
        }
    }
}
