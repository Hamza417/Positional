package app.simple.positional.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.math.MathExtensions
import app.simple.positional.util.MoonAngle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.MoonIllumination
import java.time.Instant
import java.util.*

class MoonPhaseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        onWidgetUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        onWidgetUpdate(context)
    }

    private fun onWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            val moonIllumination = MoonIllumination.compute().timezone(Calendar.getInstance().timeZone.id).on(Instant.now()).execute()

            withContext(Dispatchers.Main) {
                val views = RemoteViews(context.packageName, R.layout.widget_moon_phase)
                views.setImageViewResource(R.id.widget_moon_phase_art, MoonAngle.getMoonPhaseGraphics(MathExtensions.round(moonIllumination.phase, 2)))
                views.setTextViewText(R.id.widget_moon_phase, MoonAngle.getMoonPhase(context, moonIllumination.phase))

                val componentName = ComponentName(context, MoonPhaseWidget::class.java)
                val manager = AppWidgetManager.getInstance(context)
                manager.updateAppWidget(componentName, views)
            }
        }
    }
}