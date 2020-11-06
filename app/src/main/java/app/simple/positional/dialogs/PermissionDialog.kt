package app.simple.positional.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.views.CustomDialogFragment
import com.google.android.material.button.MaterialButton

class PermissionDialog(private var permissionCallbacks: PermissionCallbacks) : CustomDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
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
            permissionCallbacks.onGrantRequest()
            close()
        }

        close.setOnClickListener {
            close()
        }
    }

    private fun close() {
        this.dialog?.dismiss()
    }
}