package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.viewpager.widget.ViewPager
import app.simple.positional.R
import app.simple.positional.adapters.CompassDialAdapter
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_dial.*
import java.lang.ref.WeakReference

class CompassDial(compass: WeakReference<Compass>) : CustomBottomSheetDialog() {

    val weakReference = compass

    private val maxValue = 100
    private val minValue = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_dial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        compass_dial_skin.adapter = CompassDialAdapter(requireContext())
        compass_dial_indicator.attachViewPager(compass_dial_skin)
        compass_dial_skin.currentItem = CompassPreference().getDial(requireContext())

        dial_opacity.max = maxValue - minValue

        dial_opacity.progress = (CompassPreference().getDialOpacity(requireContext()) * 100f).toInt() - minValue

        compass_dial_skin.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    CompassPreference().setDial(compass_dial_skin.currentItem, requireContext())
                    weakReference.get()?.setDial(compass_dial_skin.currentItem)
                }
            }
        })

        dial_opacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                CompassPreference().setDialOpacity((progress + minValue) / 100f, requireContext())
                weakReference.get()?.setDialAlpha(progress + minValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }
}