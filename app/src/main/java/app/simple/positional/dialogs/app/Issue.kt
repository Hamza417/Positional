package app.simple.positional.dialogs.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_issue.*

class Issue : CustomBottomSheetDialog() {

    fun newInstance(): Issue {
        return Issue()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_issue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email_me.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf("hamzarizwan243@gmail.com"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Issue in Positional app")
            email.putExtra(Intent.EXTRA_TEXT, "Dear Sir,\n\nI think I found a problem in Positional app\n\n(Describe You Issue Here, Remember every suggestion is good :))")
            startActivity(Intent.createChooser(email, "Send issue"))
        }

        issue_on_github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional/issues/new")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}