package app.simple.positional.activities.subacitivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.ui.subpanels.Measures

class MeasuresActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.sub_container, Measures.newInstance(), Measures.TAG)
                    .commit()
        }
    }
}
