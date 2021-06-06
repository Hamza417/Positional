package app.simple.positional.ui.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.dialogs.miscellaneous.HtmlViewer
import app.simple.positional.licensing.*
import app.simple.positional.preferences.MainPreferences.setLicenseStatus
import app.simple.positional.util.setTextAnimation

class License : Fragment(), LicenseCheckerCallback {

    private lateinit var licenseLoader: ImageView
    private lateinit var licenseStatus: AppCompatTextView

    private var base64PublicKey = ""
    private var packageName = "a p p . s i m p l e . p o s i t i o n a l"

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var licenceStatusCallback: LicenceStatusCallback
    private var mLicenseCheckerCallback: LicenseCheckerCallback? = null
    private var mChecker: LicenseChecker? = null

    private val salt = byteArrayOf(-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_license, container, false)

        licenseLoader = view.findViewById(R.id.licence_loader)
        licenseStatus = view.findViewById(R.id.licence_status)

        return view
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        licenceStatusCallback = requireActivity() as LicenceStatusCallback
        licenseLoader.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_infinte))
        base64PublicKey = getString(R.string.licensing_key)
        val deviceId: String = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)

        mLicenseCheckerCallback = this

        mChecker = LicenseChecker(
                requireContext(), ServerManagedPolicy(requireContext(),
                AESObfuscator(salt, requireActivity().packageName, deviceId)),
                base64PublicKey)

        mChecker!!.checkAccess(this)
    }

    override fun allow(reason: Int) {
        handler.post {
            try {
                licenseStatus.setTextAnimation(getString(R.string.license_successful), 500)
                runHandler()
                setLicenseStatus(true)
            } catch (ignored: NullPointerException) {
            }
        }
    }

    override fun doNotAllow(reason: Int) {
        when (reason) {
            Policy.NOT_LICENSED -> {
                showDoNotAllowScreen(getString(R.string.license_failed))
            }
            else -> {
                /* no-op */
            }
        }
    }

    private fun showDoNotAllowScreen(error: String) {
        handler.post {
            try {
                licenseStatus.setTextAnimation(error, 500)
                licenseLoader.visibility = View.GONE
                HtmlViewer.newInstance("license_failed").show(childFragmentManager, "license_failed")
            } catch (ignored: NullPointerException) {
            }
        }
    }

    override fun applicationError(errorCode: Int) {
        handler.post {
            try {
                if (requireContext().packageName == packageName.replace("\\s".toRegex(), "")) {
                    licenseStatus.setTextAnimation(getString(R.string.error), 500)
                    runHandler()
                } else {
                    showDoNotAllowScreen("package mismatched: ${requireContext().packageName}")
                }
            } catch (ignored: NullPointerException) {
            }
        }
    }

    private fun runHandler() {
        handler.postDelayed({
            licenceStatusCallback.onLicenseCheckCompletion()
        }, 2000)
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onDestroy() {
        mChecker!!.onDestroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun newInstance(): License {
            return License()
        }
    }
}
