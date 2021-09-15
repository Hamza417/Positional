package app.simple.positional.activities.main

import android.content.Intent
import android.os.Bundle
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.preferences.FragmentPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MainPreferences.getLicenceStatus
import app.simple.positional.ui.launcher.License
import app.simple.positional.ui.launcher.SplashScreen

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
                    .replace(R.id.app_container, License.newInstance(), "license")
                    .commit()
        }
    }

    private fun setShortcutScreen() {
        if (intent.action == null) return
        when (intent.action) {
            "open_clock" -> {
                setScreenValue(0, "clock")
            }
            "open_compass" -> {
                setScreenValue(1, "compass")
            }
            "action_map_panel_full",
            "open_gps" -> {
                setScreenValue(2, "location")
            }
            "open_level" -> {
                setScreenValue(3, "level")
            }
        }
    }

    private fun setScreenValue(value: Int, tag: String) {
        FragmentPreferences.setCurrentPage(value)
        FragmentPreferences.setCurrentTag(tag)
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
                    .replace(R.id.app_container, SplashScreen.newInstance(), "launcher")
                    .commit()
        }
    }
}
