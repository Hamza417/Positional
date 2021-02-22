package app.simple.positional.views

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import app.simple.positional.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CustomDialogFragment : DialogFragment(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()

        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.width = FrameLayout.LayoutParams.MATCH_PARENT
        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)
        window.setDimAmount(0.5f)
        window.attributes.gravity = Gravity.CENTER
        window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 85f).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
