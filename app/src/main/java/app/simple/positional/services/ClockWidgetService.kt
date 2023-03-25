package app.simple.positional.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.constants.ClockSkinsConstants
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getHoursInDegreesFor24
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.model.ClockModel
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.BitmapHelper.rotateBitmap
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

abstract class ClockWidgetService : Service() {

    private var isScreenOn = true
    private val imageSize = 500
    private val serviceChannelId = "widget_channel"
    private val serviceChannelName = "Widget Channel"

    internal var contextThemeWrapper: ContextThemeWrapper? = null
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
        super.onCreate()

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
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        removeCallbacks()
                    }
                }
            }
        }

        registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        removeCallbacks()
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val clockWidgetRunnable = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.Default).launch {
                contextThemeWrapper = ContextThemeWrapper(applicationContext, getAccentTheme())

                val zonedDateTime = ZonedDateTime.now()
                val face = contextThemeWrapper!!.getSharedPreferences(SharedPreferences.preferences, Context.MODE_PRIVATE)
                        .getBoolean(ClockPreferences.is24HourFace, false)

                val needleSkins = ClockSkinsConstants.clockNeedleSkins[contextThemeWrapper!!.getSharedPreferences(
                        SharedPreferences.preferences, Context.MODE_PRIVATE).getInt(ClockPreferences.clockNeedle, 0)]

                val hour = rotateBitmap(
                        needleSkins[0].toBitmap(contextThemeWrapper!!, imageSize),
                        if (face) {
                            getHoursInDegreesFor24(zonedDateTime)
                        } else {
                            getHoursInDegrees(zonedDateTime)
                        })

                val minute = rotateBitmap(needleSkins[1].toBitmap(contextThemeWrapper!!, imageSize), getMinutesInDegrees(zonedDateTime))
                val second = rotateBitmap(needleSkins[2].toBitmap(contextThemeWrapper!!, imageSize), getSecondsInDegrees(zonedDateTime, false))

                val trail = rotateBitmap(R.drawable.clock_trail.toBitmap(contextThemeWrapper!!, imageSize), getSecondsInDegrees(zonedDateTime, false))
                val dayNight = contextThemeWrapper!!.getDayNightIndicator()
                val faceBitmap = if (face) {
                    R.drawable.clock_face_24.toBitmap(contextThemeWrapper!!, imageSize)
                } else {
                    R.drawable.clock_face.toBitmap(contextThemeWrapper!!, imageSize)
                }

                withContext(Dispatchers.Main) {
                    val clockModel = ClockModel()

                    clockModel.hour = hour
                    clockModel.minute = minute
                    clockModel.second = second
                    clockModel.dayNight = dayNight
                    clockModel.face = faceBitmap
                    clockModel.trail = trail
                    clockModel.date = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM dd"))

                    onDataGenerated(clockModel)

                    hour!!.recycle()
                    minute!!.recycle()
                    second!!.recycle()
                    faceBitmap.recycle()
                    dayNight!!.recycle()
                    trail!!.recycle()
                }
            }

            handler.postDelayed(this, 1000L)
        }
    }

    private fun Context.getDayNightIndicator(): Bitmap? {
        return when {
            ZonedDateTime.now().hour < 7 || ZonedDateTime.now().hour > 18 -> {
                R.drawable.ic_night.toBitmapKeepingSize(this, 2)
            }
            ZonedDateTime.now().hour < 18 || ZonedDateTime.now().hour > 6 -> {
                R.drawable.ic_day.toBitmapKeepingSize(this, 2)
            }
            else -> {
                null
            }
        }
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
        val channel = NotificationChannel(serviceChannelId, serviceChannelName, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return serviceChannelId
    }

    /**
     * Removes all handler callbacks
     */
    private fun removeCallbacks() {
        handler.removeCallbacks(clockWidgetRunnable)
        handler.removeCallbacksAndMessages(null)
    }

    private fun getAccentTheme(): Int {
        return when (applicationContext.getSharedPreferences(SharedPreferences.preferences, Context.MODE_PRIVATE).getInt(MainPreferences.accentColor, 0)) {
            ContextCompat.getColor(baseContext, R.color.positional) -> {
                R.style.Positional
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                R.style.Blue
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                R.style.BlueGrey
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                R.style.DarkBlue
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                R.style.Red
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                R.style.Green
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                R.style.Orange
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                R.style.Purple
            }
            ContextCompat.getColor(baseContext, R.color.yellow) -> {
                R.style.Yellow
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                R.style.CaribbeanGreen
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                R.style.PersianGreen
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                R.style.Amaranth
            }
            ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                R.style.IndianRed
            }
            ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                R.style.LightCoral
            }
            ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                R.style.PinkFlare
            }
            ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                R.style.MakeupTan
            }
            ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                R.style.EggYellow
            }
            ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                R.style.MediumGreen
            }
            ContextCompat.getColor(baseContext, R.color.olive) -> {
                R.style.Olive
            }
            ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                R.style.Copperfield
            }
            ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                R.style.MineralGreen
            }
            ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                R.style.Lochinvar
            }
            ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                R.style.BeachGrey
            }
            else -> {
                R.style.Positional
            }
        }
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

    /**
     * Post data to all subclasses
     */
    abstract fun onDataGenerated(clockModel: ClockModel)
}
