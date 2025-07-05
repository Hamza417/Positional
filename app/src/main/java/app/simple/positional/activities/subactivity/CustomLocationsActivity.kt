package app.simple.positional.activities.subactivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.ui.subpanels.CustomLocation
import app.simple.positional.util.ConditionUtils.isNull

class CustomLocationsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.sub_container, CustomLocation.newInstance(), "custom_location")
                    .commit()
        }
    }
}