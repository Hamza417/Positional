package app.simple.positional.services

import android.app.*
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
import app.simple.positional.math.TimeConverter.getHoursInDegreesFor24
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.singleton.SharedPreferences
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
            postCallbacks()
            widgetNotification()
        }

        return super.onStartCommand(intent, flags, startId)
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
                        postCallbacks()
                        widgetNotification()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        stopForeground(true)
                        removeCallbacks()
                    }
                }
            }
        }

        registerReceiver(mReceiver, filter)

        super.onCreate()
    }

    override fun onDestroy() {
        removeCallbacks()
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val clockWidgetRunnable = object : Runnable {
        @Suppress("SameParameterValue")
        override fun run() {
            CoroutineScope(Dispatchers.Default).launch {
                val zonedDateTime = ZonedDateTime.now()

                val hour = rotateBitmap(
                        R.drawable.widget_needle_hour.toBitmap(applicationContext, imageSize),
                        if (applicationContext.getSharedPreferences(SharedPreferences.preferences, Context.MODE_PRIVATE)
                                .getBoolean(ClockPreferences.is24HourFace, false)) {
                            getHoursInDegreesFor24(zonedDateTime)
                        } else {
                            getHoursInDegrees(zonedDateTime)
                        })

                val minute = rotateBitmap(R.drawable.widget_needle_minute.toBitmap(applicationContext, imageSize), getMinutesInDegrees(zonedDateTime))
                val second = rotateBitmap(R.drawable.widget_needle_seconds.toBitmap(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime, false))
                val trail = rotateBitmap(R.drawable.widget_clock_trail.toBitmap(applicationContext, imageSize), getSecondsInDegrees(zonedDateTime, false))
                val dayNight = getDayNightIndicator()

                withContext(Dispatchers.Main) {
                    val views = RemoteViews(applicationContext.packageName, R.layout.widget_clock)

                    views.setImageViewBitmap(R.id.widget_hour, hour)
                    views.setImageViewBitmap(R.id.widget_minutes, minute)
                    views.setImageViewBitmap(R.id.widget_seconds, second)
                    views.setImageViewBitmap(R.id.widget_sweep_seconds, trail)
                    views.setImageViewResource(R.id.widget_day_night_indicator, dayNight)
                    views.setImageViewResource(
                            R.id.widget_clock_face,
                            if (ClockPreferences.isClockFace24Hour()) R.drawable.widget_clock_face_24 else R.drawable.widget_clock_face)

                    views.setOnClickPendingIntent(R.id.clock_widget_wrapper, getPendingSelfIntent(applicationContext, "open_clock"))

                    val componentName = ComponentName(applicationContext, ClockWidget::class.java)
                    val manager = AppWidgetManager.getInstance(applicationContext)
                    manager.updateAppWidget(componentName, views)

                    hour!!.recycle()
                    minute!!.recycle()
                    second!!.recycle()
                    trail!!.recycle()
                }
            }

            handler.postDelayed(this, 1000L)
        }
    }

    private fun getDayNightIndicator(): Int {
        if (ZonedDateTime.now().hour < 7 || ZonedDateTime.now().hour > 18) {
            return R.drawable.widget_ic_night
        } else if (ZonedDateTime.now().hour < 18 || ZonedDateTime.now().hour > 6) {
            return R.drawable.widget_ic_day
        }

        return R.drawable.widget_ic_day
    }

    private fun widgetNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationBuilder = NotificationCompat.Builder(applicationContext, createNotificationChannel())
            val notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_place_notification)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSubText(getString(R.string.clock_widget_notification))
                    .setContentText(getString(R.string.notification_desc))
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

    /**
     * Removes all handler callbacks
     */
    private fun removeCallbacks() {
        handler.removeCallbacks(clockWidgetRunnable)
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Starts any runnable that were removed from the queue
     * This function will remove any other pending callbacks
     * before posting and new one to make sure same
     * callback does not get posted multiple times
     */
    private fun postCallbacks() {
        removeCallbacks()
        handler.post(clockWidgetRunnable)
    }

    private fun getPendingSelfIntent(context: Context, @Suppress("SameParameterValue") action: String): PendingIntent? {
        val intent = Intent(context, ClockWidget::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}
