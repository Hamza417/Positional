package app.simple.positional.activities.service

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.util.PermissionUtils.isIgnoringBatteryOptimizations

class BatteryOptimizationActivity : BaseActivity() {

    private lateinit var enable: DynamicRippleButton
    private var appWidgetCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_optimization)

        enable = findViewById(R.id.grant)

        appWidgetCode = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (isIgnoringBatteryOptimizations()) {
            close()
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
            enable.setText(R.string.button_grant)
        }
    }

    private fun close() {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetCode)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}