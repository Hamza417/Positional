package app.simple.positional.activities.main

import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.preference.MainPreferences.getLicenceStatus
import app.simple.positional.preference.MainPreferences.isDayNightOn
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.theme.setAppTheme
import app.simple.positional.ui.License
import app.simple.positional.ui.SplashScreen
import app.simple.positional.util.LocaleHelper

class LauncherActivity : BaseActivity(), LicenceStatusCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferences.init(context = applicationContext)
        LocaleHelper.autoSystemLanguageString = resources.getString(R.string.auto_system_default_language)

        if (isDayNightOn()) {
            setAppTheme(4)
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

        setContentView(R.layout.activity_launcher)

        if (getLicenceStatus() || BuildConfig.FLAVOR == "lite" || BuildConfig.DEBUG) {
            onLicenseCheckCompletion()
        } else {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.launcher_act, License().newInstance(), "license")
                    .commit()
        }
    }

    override fun onLicenseCheckCompletion() {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                .replace(R.id.launcher_act, SplashScreen().newInstance(), "launcher")
                .commit()
    }
}
