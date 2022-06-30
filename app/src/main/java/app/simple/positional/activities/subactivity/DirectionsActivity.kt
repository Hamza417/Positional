package app.simple.positional.activities.subactivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.ui.subpanels.Directions
import app.simple.positional.util.ConditionUtils.isNull

class DirectionsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.sub_container, Directions.newInstance())
                    .commit()
        }
    }
}