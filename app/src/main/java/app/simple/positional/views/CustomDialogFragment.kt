package app.simple.positional.views

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import app.simple.positional.R

@Suppress("deprecation")
open class CustomDialogFragment : DialogFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()

        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.width = FrameLayout.LayoutParams.MATCH_PARENT
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)
        window.setDimAmount(0.75f)
        window.attributes.gravity = Gravity.CENTER
        window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 85f).toInt()
    }
}