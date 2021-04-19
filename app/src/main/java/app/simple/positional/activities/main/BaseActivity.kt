package app.simple.positional.activities.main

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.ContextUtils
import app.simple.positional.util.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBaseContext: Context) {
        /**
         * Initialize [SharedPreferences] singleton here
         */
        SharedPreferences.init(newBaseContext)
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, MainPreferences.getAppLanguage()!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Sets the graphics to 8bit by default
         */
        window.setFormat(PixelFormat.RGBA_8888)

        /**
         * Checks if keep screen on flag is on
         * and updates the flags accordingly
         */
        if (MainPreferences.isScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleHelper.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0])
    }
}
