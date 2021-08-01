package app.simple.positional.activities.subactivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.activities.main.BaseActivity
import app.simple.positional.ui.subpanels.TimeZones
import app.simple.positional.util.ConditionUtils.isNull

class TimezonePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.sub_container, TimeZones.newInstance(), "timezone")
                    .commit()
        }
    }
}