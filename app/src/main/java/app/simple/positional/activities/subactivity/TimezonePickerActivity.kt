package app.simple.positional.activities.subactivity

import android.os.Bundle
import android.widget.Toast
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.ui.subpanels.TimeZones
import app.simple.positional.util.ConditionUtils.isNull

class TimezonePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (BuildConfig.FLAVOR == "lite") {
            Toast.makeText(this,
                    "Oops! This feature is not supported in this build. Purchase full version to access timezones.",
                    Toast.LENGTH_SHORT).show()

            finishAndRemoveTask()
        }

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.sub_container, TimeZones.newInstance(), "timezone")
                    .commit()
        }
    }
}