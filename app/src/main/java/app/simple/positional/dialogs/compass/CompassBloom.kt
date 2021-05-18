package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import app.simple.positional.R
import app.simple.positional.adapters.BloomSkinAdapter
import app.simple.positional.constants.CompassBloom
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.CompassPreferences
import app.simple.positional.util.LocaleHelper

class CompassBloom : CustomBottomSheetDialogFragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_bloom, container, false)

        viewPager = view.findViewById(R.id.bloom_skin_view_pager)
        textView = view.findViewById(R.id.current_bloom_theme)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.adapter = BloomSkinAdapter(requireContext())
        viewPager.currentItem = CompassPreferences.getFlowerBloomTheme()

        textView.text = String.format(
                locale = LocaleHelper.getAppLocale(),
                format = "${viewPager.currentItem + 1}/${CompassBloom.compassBloomRes.size}"
        )

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                /* no-op */
            }

            override fun onPageSelected(position: Int) {
                textView.text = String.format(
                        locale = LocaleHelper.getAppLocale(),
                        format = "${position + 1}/${CompassBloom.compassBloomRes.size}"
                )
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    CompassPreferences.setFlowerBloom(viewPager.currentItem)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isDestroyed) {
            CompassMenu().show(parentFragmentManager, "compass_menu")
        }
    }
}
