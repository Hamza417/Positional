package app.simple.positional.activities.subactivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.ui.subpanels.HtmlViewer
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull

class WebPageViewerActivity : BaseActivity() {

    private var source: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        source = if (intent.extras.isNotNull()) {
            intent.extras!!.getString("source", "null")
        } else {
            "null"
        }

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.sub_container, HtmlViewer.newInstance(source), "web_page")
                    .commit()
        }
    }
}