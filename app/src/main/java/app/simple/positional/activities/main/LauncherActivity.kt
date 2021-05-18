package app.simple.positional.activities.main

import android.content.Intent
import android.os.Bundle
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.preference.MainPreferences.getLicenceStatus
import app.simple.positional.ui.License
import app.simple.positional.ui.SplashScreen

class LauncherActivity : BaseActivity(), LicenceStatusCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShortcutScreen()

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

    private fun setShortcutScreen() {
        if (intent.action == null) return
        when (intent.action) {
            "open_clock" -> {
                setScreenValue(0)
            }
            "open_compass" -> {
                setScreenValue(1)
            }
            "action_map_panel_full",
            "open_gps" -> {
                setScreenValue(2)
            }
            "open_level" -> {
                setScreenValue(3)
            }
        }
    }

    private fun setScreenValue(value: Int) {
        FragmentPreferences.setCurrentPage(value)
    }

    override fun onLicenseCheckCompletion() {
        if (MainPreferences.getSkipSplashScreen()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.action = this.intent.action
            startActivity(intent)
            finish()
        } else {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.launcher_act, SplashScreen.newInstance(), "launcher")
                    .commit()
        }
    }
}
