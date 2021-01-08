package app.simple.positional.dialogs.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.views.CustomBottomSheetDialog
import java.io.File

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

        view.findViewById<Button>(R.id.email_me).setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf("hamzarizwan243@gmail.com"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Issue in Positional app")
            //email.putExtra(Intent.EXTRA_STREAM, getPreferenceFile())
            email.putExtra(Intent.EXTRA_TEXT, "" +
                    "I think I found a problem in Positional app," +
                    "\n\n" +
                    "(Describe your issue here in your language, Remember every suggestion is good :))" +
                    "\n\n" +
                    "App Version: ${resources.getString(R.string.versionName)}\n" +
                    "Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})\n" +
                    "API: ${Build.VERSION.SDK_INT}")
            startActivity(Intent.createChooser(email, "Send Issue"))
        }

        view.findViewById<Button>(R.id.issue_on_github).setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional/issues/new")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun getPreferenceFile(): File? {
        println(requireContext().filesDir.path + "${BuildConfig.APPLICATION_ID}/shared_prefs/Preferences.xml")
        return try {
            File(requireContext().filesDir.path + "${BuildConfig.APPLICATION_ID}/shared_prefs/Preferences.xml")
        } catch (e: NullPointerException) {
            null
        }
    }
}