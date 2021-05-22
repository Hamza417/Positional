package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import app.simple.positional.R
import app.simple.positional.adapters.ClockNeedleSkinsAdapter
import app.simple.positional.constants.ClockSkinsConstants
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.util.LocaleHelper

class ClockNeedle : CustomBottomSheetDialogFragment() {

    private lateinit var needleSkin: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_needle_skins, container, false)
        needleSkin = view.findViewById(R.id.needle_skin)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        needleSkin.adapter = ClockNeedleSkinsAdapter(requireContext())
        needleSkin.currentItem = ClockPreferences.getClockNeedleTheme()

        view.findViewById<TextView>(R.id.current_clock_theme).text = String.format(
                locale = LocaleHelper.getAppLocale(),
                format = "${needleSkin.currentItem + 1}/${ClockSkinsConstants.clockNeedleSkins.size}"
        )

        needleSkin.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                /* no-op */
            }

            override fun onPageSelected(position: Int) {
                view.findViewById<TextView>(R.id.current_clock_theme).text = String.format(
                        locale = LocaleHelper.getAppLocale(),
                        format = "${position + 1}/${ClockSkinsConstants.clockNeedleSkins.size}"
                )
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    ClockPreferences.setClockNeedleTheme(needleSkin.currentItem)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isDestroyed) {
            ClockMenu.newInstance().show(parentFragmentManager, "clock_menu")
        }
    }

    companion object {
        fun newInstance(): ClockNeedle {
            val args = Bundle()
            val fragment = ClockNeedle()
            fragment.arguments = args
            return fragment
        }
    }
}
