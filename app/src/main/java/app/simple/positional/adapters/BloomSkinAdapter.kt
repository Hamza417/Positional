package app.simple.positional.adapters

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import app.simple.positional.R
import app.simple.positional.constants.CompassBloom.compassBloomRes

class BloomSkinAdapter(context: Context) : PagerAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return compassBloomRes.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout: View = layoutInflater.inflate(R.layout.adapter_bloom_skin, container, false)

        imageLayout.findViewById<ImageView>(R.id.adapter_bloom_one).setImageResource(compassBloomRes[position])
        imageLayout.findViewById<ImageView>(R.id.adapter_bloom_two).setImageResource(compassBloomRes[position])
        imageLayout.findViewById<ImageView>(R.id.adapter_bloom_three).setImageResource(compassBloomRes[position])
        imageLayout.findViewById<ImageView>(R.id.adapter_bloom_four).setImageResource(compassBloomRes[position])

        container.addView(imageLayout, 0)
        return imageLayout
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    override fun saveState(): Parcelable? {
        return null
    }
}
