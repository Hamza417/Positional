package app.simple.positional.dialogs.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.positional.R
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.preference.MainPreferences.setShowPermissionDialog
import app.simple.positional.views.CustomBottomSheetDialog

class PermissionDialog : CustomBottomSheetDialog() {

    fun newInstance(): PermissionDialog {
        return PermissionDialog()
    }

    private var permissionCallbacks: PermissionCallbacks? = null
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        permissionCallbacks = try {
            requireActivity() as PermissionCallbacks
        } catch (e: ClassCastException) {
            null
        }
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_permission_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.permissions_webview)
        webView.setBackgroundColor(0)

        val grant: Button = view.findViewById(R.id.grant)
        val close: Button = view.findViewById(R.id.close)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            Toast.makeText(requireContext(), "If you are having trouble viewing this make sure you are using the latest WebView", Toast.LENGTH_LONG).show()
        }

        webView.loadUrl("file:///android_asset/required_permissions.html")

        grant.setOnClickListener {
            if (permissionCallbacks != null) {
                permissionCallbacks?.onGrantRequest()
            }
            close()
        }

        close.setOnClickListener {
            close()
        }

        view.findViewById<CheckBox>(R.id.show_perm_dialog).setOnCheckedChangeListener { _, isChecked ->
            setShowPermissionDialog(isChecked)
        }
    }

    private fun close() {
        this.dialog?.dismiss()
    }
}