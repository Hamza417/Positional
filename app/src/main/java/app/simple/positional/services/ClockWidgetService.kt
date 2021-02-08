package app.simple.positional.services

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import app.simple.positional.R
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.util.BitmapHelper.rotateBitmap
import app.simple.positional.util.GetVectorDrawable.getBitmapFromVectorDrawable
import app.simple.positional.widgets.ClockWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class ClockWidgetService : Service() {

    private val imageSize = 500

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val t = Thread { startJob() }
        t.start()

        return START_STICKY
    }

    private fun startJob() {
        CoroutineScope(Dispatchers.Default).launch {
            val zonedDateTime = ZonedDateTime.now()

            val hour = rotateBitmap(R.drawable.widget_needle_hour.getBitmapFromVectorDrawable(applicationContext, imageSize), getHoursInDegrees(zonedDateTime))
            val minute = rotateBitmap(R.drawable.widget_needle_minute.getBitmapFromVectorDrawable(applicationContext, imageSize), getMinutesInDegrees(zonedDateTime))
            val second = rotateBitmap(R.drawable.widget_needle_seconds.getBitmapFromVectorDrawable(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime))
            val trail = rotateBitmap(R.drawable.clock_trail.getBitmapFromVectorDrawable(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime))
            val dayNight = getDayNightIndicator()

            withContext(Dispatchers.Main) {
                val views = RemoteViews(applicationContext.packageName, R.layout.widget_clock)
                views.setImageViewBitmap(R.id.widget_hour, hour)
                views.setImageViewBitmap(R.id.widget_minutes, minute)
                views.setImageViewBitmap(R.id.widget_seconds, second)
                views.setImageViewBitmap(R.id.widget_sweep_seconds, trail)
                views.setImageViewResource(R.id.widget_day_night_indicator, dayNight)

                val manager = AppWidgetManager.getInstance(applicationContext)
                manager.updateAppWidget(ComponentName(applicationContext, ClockWidget::class.java), views)
            }
        }

        try {
            Thread.sleep(1000L)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        startJob()
    }

    private fun getDayNightIndicator(): Int {
        if (ZonedDateTime.now().hour < 7 || ZonedDateTime.now().hour > 18) {
            return R.drawable.ic_night
        } else if (ZonedDateTime.now().hour < 18 || ZonedDateTime.now().hour > 6) {
            return R.drawable.ic_day
        }

        return R.drawable.ic_day
    }
}
