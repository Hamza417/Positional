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

class Permission : CustomBottomSheetDialogFragment() {

    private var permissionCallbacks: PermissionCallbacks? = null
    private lateinit var grant: DynamicRippleButton
    private lateinit var close: DynamicRippleButton
    private lateinit var showAgain: CheckBox
    private lateinit var webView: CustomWebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permission_info, container, false)

        permissionCallbacks = requireActivity() as PermissionCallbacks

        webView = view.findViewById(R.id.permissions_webview)
        grant = view.findViewById(R.id.grant)
        close = view.findViewById(R.id.close)
        showAgain = view.findViewById(R.id.show_perm_dialog)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.loadUrl("file:///android_asset/html/required_permissions.html")

        grant.setOnClickListener {
            permissionCallbacks?.onGrantRequest()
            this.dismiss()
        }

        close.setOnClickListener {
            this.dismiss()
        }

        showAgain.setOnCheckedChangeListener { _, isChecked ->
            setShowPermissionDialog(isChecked)
        }
    }

    companion object {
        fun newInstance(): Permission {
            val args = Bundle()
            val fragment = Permission()
            fragment.arguments = args
            return fragment
        }
    }
}
