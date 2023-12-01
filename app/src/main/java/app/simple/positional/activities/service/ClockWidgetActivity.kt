package app.simple.positional.activities.service

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.util.PermissionUtils.hasNotificationPermission
import app.simple.positional.util.PermissionUtils.isIgnoringBatteryOptimizations


class ClockWidgetActivity : BaseActivity() {

    private lateinit var grantBattery: DynamicRippleButton
    private lateinit var grantNotification: DynamicRippleButton

    private var appWidgetCode = 0

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            setNotificationButton()
        } else {
            Toast.makeText(baseContext, R.string.notification_permission_desc, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock_widget)

        grantBattery = findViewById(R.id.grant_battery_optimization)
        grantNotification = findViewById(R.id.grant_notification_access)

        appWidgetCode = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (isIgnoringBatteryOptimizations() && hasNotificationPermission()) {
            close()
        }

        grantBattery.setOnClickListener {
            if (isIgnoringBatteryOptimizations()) {
                if (hasNotificationPermission()) {
                    close()
                } else {
                    Toast.makeText(baseContext, R.string.notification_permission_desc, Toast.LENGTH_LONG).show()
                }
            } else {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        grantNotification.setOnClickListener {
            if (hasNotificationPermission()) {
                if (isIgnoringBatteryOptimizations()) {
                    close()
                } else {
                    Toast.makeText(baseContext, R.string.battery_optimization_desc, Toast.LENGTH_LONG).show()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setBatteryOptimizationButton()
        setNotificationButton()
    }

    private fun setBatteryOptimizationButton() {
        if (isIgnoringBatteryOptimizations()) {
            grantBattery.setText(R.string.button_close)
        } else {
            grantBattery.setText(R.string.button_grant)
        }
    }

    private fun setNotificationButton() {
        if (hasNotificationPermission()) {
            grantNotification.setText(R.string.button_close)
        } else {
            grantNotification.setText(R.string.button_grant)
        }
    }

    private fun close() {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetCode)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}