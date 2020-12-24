package app.simple.positional.ui

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
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.fromHtml
import app.simple.positional.util.setTextAnimation
import com.google.android.vending.licensing.*

class License : Fragment(), LicenseCheckerCallback {

    fun newInstance(): License {
        return License()
    }

    private lateinit var licenseLoader: ImageView
    private lateinit var licenseStatus: TextView
    private lateinit var invalidInfoTextView: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var licenceStatusCallback: LicenceStatusCallback

    private var base64PublicKey = ""
    private var mLicenseCheckerCallback: LicenseCheckerCallback? = null
    private var mChecker: LicenseChecker? = null

    private val invalidLicenseServicePackageItemInfo = "com.android.vending.licensing.ILicensingService"
    private val lpLicenseServiceFound = "<b>Invalid license server detected</b><br><br>" +
            "Please uninstall <b>$invalidLicenseServicePackageItemInfo</b> and open the app again"
    private val invalidInfo = "" +
            "  <b><h3>Few things you can do:</h3></b><br>" +
            "  • Check if your internet is working, it is required for verification<br>" +
            "  • If you've downloaded this app from somewhere else, download it from Play Store<br>" +
            "  • If you've purchased this app and still got this issue, please contact developer to resolve this immediately"

    // Generate your own 20 random bytes, and put them here.
    private val salt = byteArrayOf(-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_license, container, false)

        licenseLoader = view.findViewById(R.id.licence_loader)
        licenseStatus = view.findViewById(R.id.licence_status)
        invalidInfoTextView = view.findViewById(R.id.invalid_info)

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
                licenseStatus.setTextAnimation("Successful :)", 500)
                runHandler(true)
            } catch (e: NullPointerException) {
            }
        }
    }

    override fun doNotAllow(reason: Int) {
        when (reason) {
            Policy.NOT_LICENSED -> {
                showDoNotAllowScreen("License Validation Failed", invalidInfo)
            }
            1337 -> {
                showDoNotAllowScreen("Invalid License Server", lpLicenseServiceFound)
            }
            else -> {
                /* no-op */
            }
        }
    }

    private fun showDoNotAllowScreen(error: String, errorInfoInHtml: String) {
        handler.post {
            try {
                licenseStatus.setTextAnimation(error, 500)
                licenseLoader.visibility = View.GONE
                invalidInfoTextView.text = fromHtml(errorInfoInHtml)
                invalidInfoTextView.visibility = View.VISIBLE
            } catch (e: NullPointerException) {
            }
        }
    }

    override fun applicationError(errorCode: Int) {
        handler.post {
            try {
                licenseStatus.setTextAnimation("Error!!", 500)
                runHandler(false)
            } catch (e: NullPointerException) {
            }
        }
    }

    private fun runHandler(value: Boolean) {
        handler.postDelayed({
            MainPreferences().setLicenseStatus(requireContext(), value)
            licenceStatusCallback.onLicenseCheckCompletion()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}