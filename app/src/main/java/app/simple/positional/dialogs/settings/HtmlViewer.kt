package app.simple.positional.dialogs.settings

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.positional.R
import app.simple.positional.util.isNetworkAvailable
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class HtmlViewer : CustomBottomSheetDialog() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_html, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.web_view)
        webView.setBackgroundColor(0)
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true

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
                getString(R.string.privacy_policy) -> {
                    webView.loadUrl("file:///android_asset/privacy_policy.html")
                }
                getString(R.string.disclaimer) -> {
                    webView.loadUrl("file:///android_asset/disclaimer.html")
                }
                getString(R.string.terms_of_use) -> {
                    webView.loadUrl("file:///android_asset/terms_and_conditions.html")
                }
                "Custom Coordinates Help" -> {
                    webView.loadUrl("file:///android_asset/custom_coordinates_help.html")
                }
                getString(R.string.permissions) -> {
                    webView.loadUrl("file:///android_asset/required_permissions.html")
                }
                "license_failed" -> {
                    webView.loadUrl("file:///android_asset/license_failed.html")
                }
                "Found Issue" -> {
                    webView.loadUrl("file:///android_asset/found_issue.html")
                }
                "Buy" -> {
                    webView.loadUrl("file:///android_asset/buy_full.html")
                }
                "translator" -> {
                    webView.loadUrl("file:///android_asset/translators.html")
                }
                "About Developer" -> {
                    webView.loadUrl("file:///android_asset/about_me/index.html")
                }
                "Change Logs" -> {
                    webView.loadUrl("file:///android_asset/loading.html")
                    downloadChangeLogs()
                }
            }
        }
    }

    private fun downloadChangeLogs() {
        @Suppress("BlockingMethodInNonBlockingContext")
        CoroutineScope(Dispatchers.IO).launch {

            if (isNetworkAvailable(requireContext())) {
                val url = "https://raw.githubusercontent.com/Hamza417/Positional/master/changelogs/changelogs.html"
                URL(url).openStream().use { input ->
                    if (File("${requireContext().cacheDir}/changelogs.html").exists()) {
                        File("${requireContext().cacheDir}/changelogs.html").delete()
                    }
                    FileOutputStream(File("${requireContext().cacheDir}/changelogs.html")).use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    webView.loadUrl(Uri.fromFile(File("${requireContext().cacheDir}/changelogs.html")).toString())
                }
            } else {
                withContext(Dispatchers.Main) {
                    webView.loadUrl("file:///android_asset/network.html")
                }
            }
        }

    }

    companion object {
        fun newInstance(string: String): HtmlViewer {
            val args = Bundle()
            args.putString("source", string)
            val fragment = HtmlViewer()
            fragment.arguments = args
            return fragment
        }
    }
}