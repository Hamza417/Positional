package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.MainPreferences
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_play_services_error.*

class PlayServiceIssue : CustomBottomSheetDialog() {

    fun newInstance(): PlayServiceIssue {
        return PlayServiceIssue()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_play_services_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        play_services_issue_positive_button.setOnClickListener {
            this.dialog?.dismiss()
        }

        play_services_issue_positive_reminder_checkbox.setOnCheckedChangeListener { _, isChecked ->
            MainPreferences().setShowPlayServiceDialog(requireContext(), isChecked)
        }
    }
}