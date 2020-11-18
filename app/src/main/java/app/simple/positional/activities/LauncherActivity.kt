package app.simple.positional.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.preference.MainPreferences
import app.simple.positional.theme.setAppTheme
import app.simple.positional.ui.Launcher
import app.simple.positional.ui.License


class LauncherActivity : AppCompatActivity(), LicenceStatusCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MainPreferences().isDayNightOn(this)) {
            setAppTheme(4)
        } else {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        setContentView(R.layout.activity_launcher)

        if (!BuildConfig.DEBUG) {
            if (MainPreferences().getLicenceStatus(this) || BuildConfig.FLAVOR == "lite") {
                onLicenseCheckCompletion()
            } else {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                        .replace(R.id.launcher_act, License().newInstance(), "license")
                        .commit()
            }
        } else {
            onLicenseCheckCompletion()
        }
    }

    override fun onLicenseCheckCompletion() {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                .replace(R.id.launcher_act, Launcher().newInstance(), "launcher")
                .commit()
    }
}