package app.simple.positional.extensions.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.positional.R
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.CompassPreferences
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.ContextUtils
import app.simple.positional.util.LocaleHelper
import app.simple.positional.util.ThemeSetter

open class BaseActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun attachBaseContext(newBaseContext: Context) {
        /**
         * Initialize [SharedPreferences] singleton here
         */
        SharedPreferences.init(newBaseContext)
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, MainPreferences.getAppLanguage()!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // StrictMode.enableDefaults()

        if (MainPreferences.isDayNightOn()) {
            ThemeSetter.setAppTheme(4)
        } else {
            val value = MainPreferences.getTheme()

            if (value != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(value)
            }
        }

        if (MainPreferences.getLaunchCount() == 0) {
            if (DateFormat.is24HourFormat(this)) {
                ClockPreferences.setDefaultClockTime(false)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.colorMode = ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
        }

        /**
         * Sets the graphics to 8bit by default
         */
        window.setFormat(PixelFormat.RGBA_8888)

        /**
         * Checks if keep screen on flag is on
         * and update the flags accordingly
         */
        if (MainPreferences.isScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        makeAppFullScreen()
        fixNavigationBarOverlap()

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleHelper.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)

        setTheme()
    }

    private fun resetLitePrefs() {
        CompassPreferences.setFlowerBloom(false)
        ClockPreferences.setClockNeedleTheme(1)
        MainPreferences.setCustomCoordinates(false)
        GPSPreferences.setPinSkin(0)
    }

    private fun setTheme() {
        if (MainPreferences.isMaterialYouAccentColor()) {
            setTheme(R.style.MaterialYou)
        } else {
            when (MainPreferences.getAccentColor()) {
                ContextCompat.getColor(baseContext, R.color.positional) -> {
                    setTheme(R.style.Positional)
                }
                ContextCompat.getColor(baseContext, R.color.blue) -> {
                    setTheme(R.style.Blue)
                }
                ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                    setTheme(R.style.BlueGrey)
                }
                ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                    setTheme(R.style.DarkBlue)
                }
                ContextCompat.getColor(baseContext, R.color.red) -> {
                    setTheme(R.style.Red)
                }
                ContextCompat.getColor(baseContext, R.color.green) -> {
                    setTheme(R.style.Green)
                }
                ContextCompat.getColor(baseContext, R.color.orange) -> {
                    setTheme(R.style.Orange)
                }
                ContextCompat.getColor(baseContext, R.color.purple) -> {
                    setTheme(R.style.Purple)
                }
                ContextCompat.getColor(baseContext, R.color.yellow) -> {
                    setTheme(R.style.Yellow)
                }
                ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                    setTheme(R.style.CaribbeanGreen)
                }
                ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                    setTheme(R.style.PersianGreen)
                }
                ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                    setTheme(R.style.Amaranth)
                }
                ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                    setTheme(R.style.IndianRed)
                }
                ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                    setTheme(R.style.LightCoral)
                }
                ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                    setTheme(R.style.PinkFlare)
                }
                ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                    setTheme(R.style.MakeupTan)
                }
                ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                    setTheme(R.style.EggYellow)
                }
                ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                    setTheme(R.style.MediumGreen)
                }
                ContextCompat.getColor(baseContext, R.color.olive) -> {
                    setTheme(R.style.Olive)
                }
                ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                    setTheme(R.style.Copperfield)
                }
                ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                    setTheme(R.style.MineralGreen)
                }
                ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                    setTheme(R.style.Lochinvar)
                }
                ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                    setTheme(R.style.BeachGrey)
                }
                else -> {
                    setTheme(R.style.Positional)
                    MainPreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.positional))
                }
            }
        }
    }

    private fun makeAppFullScreen() {
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    private fun fixNavigationBarOverlap() {
        /**
         * Root ViewGroup of this activity
         */
        val root = findViewById<FrameLayout>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            /**
             * Apply the insets as a margin to the view. Here the system is setting
             * only the bottom, left, and right dimensions, but apply whichever insets are
             * appropriate to your layout. You can also update the view padding
             * if that's more appropriate.
             */
            view.layoutParams = (view.layoutParams as LinearLayout.LayoutParams).apply {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
        }
    }

    protected fun postDelayed(delay: Long, action: () -> Unit) {
        handler.postDelayed(action, delay)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
