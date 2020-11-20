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
            val value = MainPreferences().getTheme(this)

            if (value != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(value)
            }
        }

        setContentView(R.layout.activity_launcher)

        if (MainPreferences().getLicenceStatus(this) || BuildConfig.FLAVOR == "lite") {
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
                .replace(R.id.launcher_act, Launcher().newInstance(), "launcher")
                .commit()
    }
}