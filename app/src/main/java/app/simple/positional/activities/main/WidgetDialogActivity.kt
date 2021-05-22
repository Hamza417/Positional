package app.simple.positional.activities.main

import android.os.Bundle
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.preferences.WidgetPreferences
import app.simple.positional.singleton.SharedPreferences

class WidgetDialogActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferences.init(applicationContext)

        if (WidgetPreferences.isWidgetAlertShowAgain()) {
            ErrorDialog.newInstance("Widget")
                    .show(supportFragmentManager, "widget_dialog")
        } else {
            finishAfterTransition()
        }
    }
}