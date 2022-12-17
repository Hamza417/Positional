package app.simple.positional.decorations.views

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import app.simple.positional.R
import app.simple.positional.util.StatusBarHeight

open class CustomDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()

        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.width = FrameLayout.LayoutParams.MATCH_PARENT
        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)
        window.setDimAmount(0.35f)
        window.attributes.gravity = Gravity.CENTER

        // TODO - fixe dialog height
        if (StatusBarHeight.isLandscape(requireContext())) {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 60f).toInt()
            // window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 90F).toInt()
        } else {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 75f).toInt()
            // window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 60F).toInt()
        }
    }
}
