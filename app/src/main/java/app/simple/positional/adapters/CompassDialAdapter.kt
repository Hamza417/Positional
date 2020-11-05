package app.simple.positional.adapters

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import app.simple.positional.R
import app.simple.positional.constants.compassDialSkins
import app.simple.positional.constants.compassNeedleSkins
import app.simple.positional.preference.CompassPreference
import kotlinx.android.synthetic.main.adapter_compass_dial.view.*

class CompassDialAdapter(private val context: Context) : PagerAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return compassDialSkins.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout: View = layoutInflater.inflate(R.layout.adapter_compass_dial, container, false)

        imageLayout.dial_adapter_back.setImageResource(compassDialSkins[position])
        imageLayout.dial_adapter_front.setImageResource(compassNeedleSkins[CompassPreference().getNeedle(context)])

        container.addView(imageLayout, 0)
        return imageLayout
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    override fun saveState(): Parcelable? {
        return null
    }
}