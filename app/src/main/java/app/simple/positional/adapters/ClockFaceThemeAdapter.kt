package app.simple.positional.adapters

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import app.simple.positional.R
import app.simple.positional.constants.clockFaceSkins
import app.simple.positional.constants.clockNeedleSkins
import app.simple.positional.preference.ClockPreferences
import kotlinx.android.synthetic.main.adapter_clock_face.view.*

class ClockFaceThemeAdapter(private val context: Context) : PagerAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return clockFaceSkins.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout: View = layoutInflater.inflate(R.layout.adapter_clock_face, container, false)

        imageLayout.adapter_clock_background.setImageResource(clockFaceSkins[position])
        imageLayout.adapter_clock_hour.setImageResource(clockNeedleSkins[ClockPreferences().getClockNeedleTheme(context)][0])
        imageLayout.adapter_clock_minute.setImageResource(clockNeedleSkins[ClockPreferences().getClockNeedleTheme(context)][1])
        imageLayout.adapter_clock_second.setImageResource(clockNeedleSkins[ClockPreferences().getClockNeedleTheme(context)][2])

        container.addView(imageLayout, 0)
        return imageLayout
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    override fun saveState(): Parcelable? {
        return null
    }
}