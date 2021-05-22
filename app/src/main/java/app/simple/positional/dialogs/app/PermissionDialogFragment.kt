package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import app.simple.positional.R
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomWebView
import app.simple.positional.preferences.MainPreferences.setShowPermissionDialog

class PermissionDialogFragment : CustomBottomSheetDialogFragment() {

    fun newInstance(): PermissionDialogFragment {
        return PermissionDialogFragment()
    }

    private var permissionCallbacks: PermissionCallbacks? = null
    private lateinit var webView: CustomWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        permissionCallbacks = try {
            requireActivity() as PermissionCallbacks
        } catch (e: ClassCastException) {
            null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_permission_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.permissions_webview)

        val grant: DynamicRippleButton = view.findViewById(R.id.grant)
        val close: DynamicRippleButton = view.findViewById(R.id.close)

        webView.loadUrl("file:///android_asset/html/required_permissions.html")

        grant.setOnClickListener {
            if (permissionCallbacks != null) {
                permissionCallbacks?.onGrantRequest()
            }
            this.dismiss()
        }

        close.setOnClickListener {
            this.dismiss()
        }

        view.findViewById<CheckBox>(R.id.show_perm_dialog).setOnCheckedChangeListener { _, isChecked ->
            setShowPermissionDialog(isChecked)
        }
    }
}
