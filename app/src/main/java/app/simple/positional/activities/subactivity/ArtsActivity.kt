package app.simple.positional.activities.subactivity

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import app.simple.positional.R
import app.simple.positional.activities.main.BaseActivity
import app.simple.positional.adapters.miscellaneous.ArtsAdapter
import app.simple.positional.preferences.MainPreferences

class ArtsActivity: BaseActivity() {

    private lateinit var viewPager2 : ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arts)

        viewPager2 = findViewById(R.id.arts_view_pager)

        viewPager2.adapter = ArtsAdapter()
        viewPager2.setCurrentItem(MainPreferences.getCurrentArt(), false)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                MainPreferences.setCurrentArt(position)
            }
        })
    }
}