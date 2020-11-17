package app.simple.positional.dialogs.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.positional.R
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_legal_notes.*

class LegalNotes : CustomBottomSheetDialog() {
    fun newInstance(string: String): LegalNotes {
        val args = Bundle()
        args.putString("legal", string)
        val fragment = LegalNotes()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_legal_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        web_view.setBackgroundColor(0)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                WebSettingsCompat.setForceDark(web_view.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            Toast.makeText(requireContext(), "If you are having trouble viewing this make sure you are using the latest WebView", Toast.LENGTH_LONG).show()
        }

        if (this.arguments != null) {
            when (this.requireArguments().get("legal")) {
                "Privacy Policy" -> {
                    loadWebView("file:///android_asset/privacy_policy.html")
                }
                "Disclaimer" -> {
                    loadWebView("file:///android_asset/disclaimer.html")
                }
                "Terms of Use" -> {
                    loadWebView("file:///android_asset/terms_and_conditions.html")
                }
            }
        }
    }

    private fun loadWebView(url: String) {
        web_view.loadUrl(url)
    }
}