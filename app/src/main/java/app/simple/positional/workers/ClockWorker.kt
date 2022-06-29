package app.simple.positional.workers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.simple.positional.R
import app.simple.positional.constants.ClockSkinsConstants
import app.simple.positional.math.TimeConverter
import app.simple.positional.model.ClockModel
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.BitmapHelper
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import app.simple.positional.widgets.ClockWidget4x4
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ClockWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), OnSharedPreferenceChangeListener {

    private var contextThemeWrapper: ContextThemeWrapper? = null
    private val imageSize = 500

    override fun doWork(): Result {
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        contextThemeWrapper = ContextThemeWrapper(applicationContext, getAccentTheme())
        getClockData()
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun getClockData() {
        val zonedDateTime = ZonedDateTime.now()
        val face = contextThemeWrapper!!.getSharedPreferences(SharedPreferences.preferences, Context.MODE_PRIVATE)
                .getBoolean(ClockPreferences.is24HourFace, false)

        val needleSkins = ClockSkinsConstants.clockNeedleSkins[contextThemeWrapper!!.getSharedPreferences(
                SharedPreferences.preferences, Context.MODE_PRIVATE).getInt(ClockPreferences.clockNeedle, 1)]

        val hour = BitmapHelper.rotateBitmap(
                needleSkins[0].toBitmap(contextThemeWrapper!!, imageSize),
                if (face) {
                    TimeConverter.getHoursInDegreesFor24(zonedDateTime)
                } else {
                    TimeConverter.getHoursInDegrees(zonedDateTime)
                })

        val minute = BitmapHelper.rotateBitmap(needleSkins[1].toBitmap(contextThemeWrapper!!, imageSize), TimeConverter.getMinutesInDegrees(zonedDateTime))
        val second = BitmapHelper.rotateBitmap(needleSkins[2].toBitmap(contextThemeWrapper!!, imageSize), TimeConverter.getSecondsInDegrees(zonedDateTime, false))

        val trail = BitmapHelper.rotateBitmap(R.drawable.clock_trail.toBitmap(contextThemeWrapper!!, imageSize), TimeConverter.getSecondsInDegrees(zonedDateTime, false))
        val dayNight = contextThemeWrapper!!.getDayNightIndicator()
        val faceBitmap = if (face) {
            R.drawable.clock_face_24.toBitmap(contextThemeWrapper!!, imageSize)
        } else {
            R.drawable.clock_face.toBitmap(contextThemeWrapper!!, imageSize)
        }

        val clockModel = ClockModel()

        clockModel.hour = hour
        clockModel.minute = minute
        clockModel.second = second
        clockModel.dayNight = dayNight
        clockModel.face = faceBitmap
        clockModel.trail = trail
        clockModel.date = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM dd"))

        updateGeneratedData(clockModel)

        hour!!.recycle()
        minute!!.recycle()
        second!!.recycle()
        faceBitmap.recycle()
        dayNight!!.recycle()
        trail!!.recycle()
    }

    private fun updateGeneratedData(clockModel: ClockModel) {
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

    private fun getAccentTheme(): Int {
        return when (applicationContext.getSharedPreferences(SharedPreferences.preferences, Context.MODE_PRIVATE).getInt(MainPreferences.accentColor, 0)) {
            ContextCompat.getColor(applicationContext, R.color.positional) -> {
                R.style.Positional
            }
            ContextCompat.getColor(applicationContext, R.color.blue) -> {
                R.style.Blue
            }
            ContextCompat.getColor(applicationContext, R.color.blueGrey) -> {
                R.style.BlueGrey
            }
            ContextCompat.getColor(applicationContext, R.color.darkBlue) -> {
                R.style.DarkBlue
            }
            ContextCompat.getColor(applicationContext, R.color.red) -> {
                R.style.Red
            }
            ContextCompat.getColor(applicationContext, R.color.green) -> {
                R.style.Green
            }
            ContextCompat.getColor(applicationContext, R.color.orange) -> {
                R.style.Orange
            }
            ContextCompat.getColor(applicationContext, R.color.purple) -> {
                R.style.Purple
            }
            ContextCompat.getColor(applicationContext, R.color.yellow) -> {
                R.style.Yellow
            }
            ContextCompat.getColor(applicationContext, R.color.caribbeanGreen) -> {
                R.style.CaribbeanGreen
            }
            ContextCompat.getColor(applicationContext, R.color.persianGreen) -> {
                R.style.PersianGreen
            }
            ContextCompat.getColor(applicationContext, R.color.amaranth) -> {
                R.style.Amaranth
            }
            ContextCompat.getColor(applicationContext, R.color.indian_red) -> {
                R.style.IndianRed
            }
            ContextCompat.getColor(applicationContext, R.color.light_coral) -> {
                R.style.LightCoral
            }
            ContextCompat.getColor(applicationContext, R.color.pink_flare) -> {
                R.style.PinkFlare
            }
            ContextCompat.getColor(applicationContext, R.color.makeup_tan) -> {
                R.style.MakeupTan
            }
            ContextCompat.getColor(applicationContext, R.color.egg_yellow) -> {
                R.style.EggYellow
            }
            ContextCompat.getColor(applicationContext, R.color.medium_green) -> {
                R.style.MediumGreen
            }
            ContextCompat.getColor(applicationContext, R.color.olive) -> {
                R.style.Olive
            }
            ContextCompat.getColor(applicationContext, R.color.copperfield) -> {
                R.style.Copperfield
            }
            ContextCompat.getColor(applicationContext, R.color.mineral_green) -> {
                R.style.MineralGreen
            }
            ContextCompat.getColor(applicationContext, R.color.lochinvar) -> {
                R.style.Lochinvar
            }
            ContextCompat.getColor(applicationContext, R.color.beach_grey) -> {
                R.style.BeachGrey
            }
            else -> {
                R.style.Positional
            }
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

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.accentColor -> {
                contextThemeWrapper = ContextThemeWrapper(applicationContext, getAccentTheme())
            }
        }
    }
}