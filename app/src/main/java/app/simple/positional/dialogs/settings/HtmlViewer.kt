package app.simple.positional.dialogs.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.positional.R
import app.simple.positional.views.CustomBottomSheetDialog

class HtmlViewer : CustomBottomSheetDialog() {

    private lateinit var webView: WebView

    fun newInstance(string: String): HtmlViewer {
        val args = Bundle()
        args.putString("source", string)
        val fragment = HtmlViewer()
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

        webView = view.findViewById(R.id.web_view)
        webView.setBackgroundColor(0)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            Toast.makeText(requireContext(), "If you are having trouble viewing this make sure you are using the latest WebView", Toast.LENGTH_LONG).show()
        }

        if (this.arguments != null) {
            when (this.requireArguments().get("source")) {
                "Privacy Policy" -> {
                    loadWebView("file:///android_asset/privacy_policy.html")
                }
                "Disclaimer" -> {
                    loadWebView("file:///android_asset/disclaimer.html")
                }
                "Terms of Use" -> {
                    loadWebView("file:///android_asset/terms_and_conditions.html")
                }
                "Custom Coordinates Help" -> {
                    loadWebView("file:///android_asset/custom_coordinates_help.html")
                }
                "Permissions" -> {
                    loadWebView("file:///android_asset/required_permissions.html")
                }
            }
        }
    }

    private fun loadWebView(url: String) {
        webView.loadUrl(url)
    }
}