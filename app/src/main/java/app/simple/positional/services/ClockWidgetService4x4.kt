package app.simple.positional.services

import android.appwidget.AppWidgetManager
import android.content.*
import android.os.IBinder
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.model.ClockModel
import app.simple.positional.widgets.ClockWidget4x4

class ClockWidgetService4x4 : ClockWidgetService() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDataGenerated(clockModel: ClockModel) {
        super.onDataGenerated(clockModel)

        val views = RemoteViews(contextThemeWrapper?.packageName, R.layout.widget_clock)

        views.setImageViewBitmap(R.id.widget_hour, clockModel.hour)
        views.setImageViewBitmap(R.id.widget_minutes, clockModel.minute)
        views.setImageViewBitmap(R.id.widget_seconds, clockModel.second)
        views.setImageViewBitmap(R.id.widget_sweep_seconds, clockModel.trail)
        views.setImageViewBitmap(R.id.widget_day_night_indicator, clockModel.dayNight)
        views.setImageViewBitmap(R.id.widget_clock_face, clockModel.face)

        views.setTextViewText(R.id.widget_date, clockModel.date)

        val componentName = ComponentName(contextThemeWrapper!!, ClockWidget4x4::class.java)
        val manager = AppWidgetManager.getInstance(contextThemeWrapper)
        manager.updateAppWidget(componentName, views)
    }
}
