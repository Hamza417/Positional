package app.simple.positional.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Display
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import app.simple.positional.R
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.util.BitmapHelper.rotateBitmap
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.widgets.ClockWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class ClockWidgetService : Service() {

    private var isScreenOn = true
    private val imageSize = 400
    private val serviceChannelId = "widget_channel"
    private val serviceChannelName = "Widget Channel"

    private var mReceiver: BroadcastReceiver? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        isScreenOn = (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays[0].state == Display.STATE_ON

        if (isScreenOn) {
            handler.post(clockWidgetRunnable)
            widgetNotification()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOn = true
                        handler.post(clockWidgetRunnable)
                        widgetNotification()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        stopForeground(true)
                        handler.removeCallbacks(clockWidgetRunnable)
                    }
                }
            }
        }

        registerReceiver(mReceiver, filter)

        super.onCreate()
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockWidgetRunnable)
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val clockWidgetRunnable = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.Default).launch {
                val zonedDateTime = ZonedDateTime.now()

                val hour = rotateBitmap(R.drawable.widget_needle_hour.toBitmap(applicationContext, imageSize), getHoursInDegrees(zonedDateTime))
                val minute = rotateBitmap(R.drawable.widget_needle_minute.toBitmap(applicationContext, imageSize), getMinutesInDegrees(zonedDateTime))
                val second = rotateBitmap(R.drawable.widget_needle_seconds.toBitmap(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime))
                val trail = rotateBitmap(R.drawable.clock_trail.toBitmap(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime))
                val dayNight = getDayNightIndicator()

                withContext(Dispatchers.Main) {
                    val views = RemoteViews(applicationContext.packageName, R.layout.widget_clock)
                    views.setImageViewBitmap(R.id.widget_hour, hour)
                    views.setImageViewBitmap(R.id.widget_minutes, minute)
                    views.setImageViewBitmap(R.id.widget_seconds, second)
                    views.setImageViewBitmap(R.id.widget_sweep_seconds, trail)
                    views.setImageViewResource(R.id.widget_day_night_indicator, dayNight)

                    val componentName = ComponentName(applicationContext, ClockWidget::class.java)
                    val manager = AppWidgetManager.getInstance(applicationContext)
                    manager.updateAppWidget(componentName, views)
                }
            }

            handler.postDelayed(this, 1000L)
        }
    }

    private fun getDayNightIndicator(): Int {
        if (ZonedDateTime.now().hour < 7 || ZonedDateTime.now().hour > 18) {
            return R.drawable.ic_night
        } else if (ZonedDateTime.now().hour < 18 || ZonedDateTime.now().hour > 6) {
            return R.drawable.ic_day
        }

        return R.drawable.ic_day
    }

    private fun widgetNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationBuilder = NotificationCompat.Builder(applicationContext, createNotificationChannel())
            val notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_place_notification)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSubText("Clock widget is running")
                    .build()
            startForeground(101, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val chan = NotificationChannel(serviceChannelId, serviceChannelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return serviceChannelId
    }
}