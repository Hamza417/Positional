package app.simple.positional.activities.subactivity

import android.os.Bundle
import android.widget.Toast
import app.simple.positional.R
import app.simple.positional.activities.main.BaseActivity
import app.simple.positional.ui.subpanels.TrailData

class TrailDataActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        if (intent.hasExtra("trail_name")) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.sub_container, TrailData.newInstance(intent.extras!!.getString("trail_name")!!))
                .commit()
        } else {
            Toast.makeText(this, "${packageName}: abnormal_launch_detected", Toast.LENGTH_SHORT).show()
        }
    }
}
