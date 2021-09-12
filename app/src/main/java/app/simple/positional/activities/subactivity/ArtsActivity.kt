package app.simple.positional.activities.subactivity

import android.os.Bundle
import app.simple.positional.R
import app.simple.positional.activities.main.BaseActivity
import app.simple.positional.adapters.miscellaneous.ArtsAdapter
import app.simple.positional.decorations.views.CustomRecyclerView

class ArtsActivity: BaseActivity() {

    private lateinit var recyclerView : CustomRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arts)

        recyclerView = findViewById(R.id.arts_recycler_view)

        recyclerView.adapter = ArtsAdapter()
    }
}