package app.simple.positional.decorations.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import app.simple.positional.R
import app.simple.positional.util.StatusBarHeight

open class CustomDialogFragment : DialogFragment() {

    private val handler = Handler(Looper.getMainLooper())

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
        window.setDimAmount(DIM_AMOUNT)
        window.attributes.gravity = Gravity.CENTER
        window.attributes.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN

        // TODO - fixe dialog height
        window.attributes.width = getWindowWidth()
    }

    open fun getWindowWidth(): Int {
        val window = dialog!!.window ?: return 0
        val displayMetrics = DisplayMetrics()

        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val widthPercentage = if (StatusBarHeight.isLandscape(requireContext())) {
            LANDSCAPE_WIDTH
        } else {
            PORTRAIT_WIDTH
        }

        return (displayMetrics.widthPixels * widthPercentage).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    fun postDelayed(delay: Long, runnable: () -> Unit) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            runnable()
        }, delay)
    }

    companion object {
        private const val PORTRAIT_WIDTH = 0.80F
        private const val LANDSCAPE_WIDTH = 0.60F
        private const val DIM_AMOUNT = 0.35F
    }
}
