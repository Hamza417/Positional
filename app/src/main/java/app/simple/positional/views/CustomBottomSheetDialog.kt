package app.simple.positional.views

import android.os.Bundle
import app.simple.positional.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class CustomBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }
}