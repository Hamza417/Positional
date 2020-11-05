package app.simple.positional.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import app.simple.positional.R
import app.simple.positional.adapters.CompassNeedleAdapter
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_needle.*
import java.lang.ref.WeakReference

class CompassNeedle(compass: WeakReference<Compass>) : CustomBottomSheetDialog() {

    val weakReference = compass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_needle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        compass_needle_skin.adapter = CompassNeedleAdapter(requireContext())
        compass_needle_indicator.attachViewPager(compass_needle_skin)
        compass_needle_skin.currentItem = CompassPreference().getNeedle(requireContext())

        compass_needle_skin.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    CompassPreference().setNeedle(compass_needle_skin.currentItem, requireContext())
                    weakReference.get()?.setNeedle(compass_needle_skin.currentItem)
                }
            }
        })
    }
}