package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewpager.widget.ViewPager
import app.simple.positional.R
import app.simple.positional.adapters.ClockNeedleSkinsAdapter
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import com.afollestad.viewpagerdots.DotsIndicator
import java.lang.ref.WeakReference

class ClockNeedle(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {

    private lateinit var needleSkin: ViewPager
    private lateinit var indicator: DotsIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_needle_skins, container, false)

        needleSkin = view.findViewById(R.id.needle_skin)
        indicator = view.findViewById(R.id.indicator)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This will prevent the underlying dialog from dimming preventing a flashy animation that can cause some issues to some users
        this.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        needleSkin.adapter = ClockNeedleSkinsAdapter(requireContext())
        indicator.attachViewPager(needleSkin)

        needleSkin.currentItem = ClockPreferences.getClockNeedleTheme()

        needleSkin.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    ClockPreferences.setClockNeedleTheme(needleSkin.currentItem)
                    clock.get()?.setNeedle(needleSkin.currentItem)
                }
            }
        })
    }
}