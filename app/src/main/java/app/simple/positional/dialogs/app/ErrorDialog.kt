package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment

class ErrorDialog : CustomBottomSheetDialogFragment() {

    private lateinit var error: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_error, container, false)

        error = view.findViewById(R.id.print_error)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        error.text = requireArguments().getString("error")
    }

    companion object {
        fun newInstance(error: String): ErrorDialog {
            val args = Bundle()
            args.putString("error", error)
            val fragment = ErrorDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
