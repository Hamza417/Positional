package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.preference.MainPreferences
import app.simple.positional.views.CustomBottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.dialog_permission_info.*

class PermissionDialog : CustomBottomSheetDialog() {

    fun newInstance(): PermissionDialog {
        return PermissionDialog()
    }

    private var permissionCallbacks: PermissionCallbacks? = null

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
        val grant: MaterialButton = view.findViewById(R.id.grant)
        val close: MaterialButton = view.findViewById(R.id.close)

        grant.setOnClickListener {
            if (permissionCallbacks != null) {
                permissionCallbacks?.onGrantRequest()
            }
            close()
        }

        close.setOnClickListener {
            close()
        }

        show_perm_dialog.setOnCheckedChangeListener { _, isChecked ->
            MainPreferences().setShowPermissionDialog(requireContext(), isChecked)
        }
    }

    private fun close() {
        this.dialog?.dismiss()
    }
}