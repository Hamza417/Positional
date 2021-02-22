package app.simple.positional.views

import android.os.Bundle
import android.view.View
import app.simple.positional.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CustomBottomSheetDialog : BottomSheetDialogFragment(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
        dialog?.window?.setDimAmount(0.5f)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog?.window?.setElevation(5f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}