package app.simple.positional.activities.service

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.extensions.activity.BaseActivity

class BatteryOptimizationActivity : BaseActivity() {

    private lateinit var enable: DynamicRippleButton
    private var appWidgeCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_optimization)

        enable = findViewById(R.id.enable)

        appWidgeCode = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (isIgnoringBatteryOptimizations()) {
            enable.setText(R.string.button_close)
        }

        enable.setOnClickListener {
            if (isIgnoringBatteryOptimizations()) {
                close()
            } else {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isIgnoringBatteryOptimizations()) {
            enable.setText(R.string.button_close)
        } else {
            enable.setText(R.string.common_google_play_services_enable_button)
        }
    }

    /**
     * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
     */
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = baseContext.packageName
        return powerManager.isIgnoringBatteryOptimizations(name)
    }

    private fun close() {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgeCode)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}