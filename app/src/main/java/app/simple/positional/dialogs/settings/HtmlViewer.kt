package app.simple.positional.dialogs.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomWebView
import app.simple.positional.util.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

class HtmlViewer : CustomBottomSheetDialogFragment() {

    private lateinit var webView: CustomWebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_html, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.web_view)

        if (this.arguments != null) {
            when (this.requireArguments().get("source")) {
                getString(R.string.privacy_policy) -> {
                    webView.loadUrl("file:///android_asset/html/privacy_policy.html")
                }
                getString(R.string.disclaimer) -> {
                    webView.loadUrl("file:///android_asset/html/disclaimer.html")
                }
                getString(R.string.terms_of_use) -> {
                    webView.loadUrl("file:///android_asset/html/terms_and_conditions.html")
                }
                "Custom Coordinates Help" -> {
                    webView.loadUrl("file:///android_asset/html/custom_coordinates_help.html")
                }
                getString(R.string.permissions) -> {
                    webView.loadUrl("file:///android_asset/html/required_permissions.html")
                }
                "Credits" -> {
                    webView.loadUrl("file:///android_asset/html/credits.html")
                }
                "license_failed" -> {
                    webView.loadUrl("file:///android_asset/html/license_failed.html")
                }
                "Found Issue" -> {
                    webView.loadUrl("file:///android_asset/html/found_issue.html")
                }
                "Buy" -> {
                    webView.loadUrl("file:///android_asset/html/buy_full.html")
                }
                "translator" -> {
                    webView.loadUrl("file:///android_asset/html/translators.html")
                }
                "Change Logs" -> {
                    webView.loadUrl("file:///android_asset/html/local_changelogs.html")
                }
                getString(R.string.internet_uses) -> {
                    webView.loadUrl("file:///android_asset/html/internet_uses.html")
                }
                "Development Status" -> {
                    webView.loadUrl("file:///android_asset/html/loading.html")
                    launch {
                        downloadChangeLogs()
                    }
                }
                getString(R.string.physical_properties) -> {
                    webView.loadUrl("file:///android_asset/html/physical_properties.html")
                }
                "Media Keys" -> {
                    webView.loadUrl("file:///android_asset/html/media_keys.html")
                }
            }
        }
    }

    private suspend fun downloadChangeLogs() {
        try {
            if (isNetworkAvailable(requireContext())) {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val url = "https://raw.githubusercontent.com/Hamza417/Positional/master/app/src/main/assets/html/changelogs.html"
                        URL(url).openStream().use { input ->
                            if (File("${context?.cacheDir}/changelogs.html").exists()) {
                                File("${context?.cacheDir}/changelogs.html").delete()
                            }
                            FileOutputStream(File("${context?.cacheDir}/changelogs.html")).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }

                webView.loadUrl(Uri.fromFile(File("${requireContext().cacheDir}/changelogs.html")).toString())

            } else {
                Toast.makeText(requireContext(), R.string.internet_connection_alert, Toast.LENGTH_SHORT).show()
                this@HtmlViewer.dismiss()
            }
        } catch (e: FileNotFoundException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            this@HtmlViewer.dismiss()
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
