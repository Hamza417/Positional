package app.simple.positional.ui.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.activities.subactivity.WebPageViewerActivity
import app.simple.positional.callbacks.LicenceStatusCallback
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.MainPreferences.setLicenseStatus
import app.simple.positional.util.TextViewUtils.setTextAnimation
import app.simple.positional.util.ViewUtils
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.LicenseViewModel

class License : Fragment() {

    private lateinit var licenseStatus: TextView
    private lateinit var retry: DynamicRippleImageButton
    private lateinit var help: DynamicRippleButton
    private lateinit var buttonsContainer: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var licenceStatusCallback: LicenceStatusCallback

    private lateinit var licenseViewModel: LicenseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_license, container, false)

        licenseStatus = view.findViewById(R.id.licence_status)
        retry = view.findViewById(R.id.retry)
        help = view.findViewById(R.id.help)
        buttonsContainer = view.findViewById(R.id.buttons_container)

        licenseViewModel = ViewModelProvider(requireActivity()).get(LicenseViewModel::class.java)

        return view
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        licenceStatusCallback = requireActivity() as LicenceStatusCallback

        licenseViewModel.allow.observe(viewLifecycleOwner, {
            licenseStatus.setTextAnimation(it, 500)
            runHandler()
            setLicenseStatus(true)
        })

        licenseViewModel.doNotAllow.observe(viewLifecycleOwner, {
            licenseStatus.setTextAnimation(it, 500)
            buttonsContainer.visible(true)
        })

        licenseViewModel.applicationError.observe(viewLifecycleOwner, {
            licenseStatus.setTextAnimation(it, 500)
            runHandler()
        })

        retry.setOnClickListener {
            licenseStatus.setTextAnimation(getString(R.string.verifying_license), 500)
            buttonsContainer.gone()
            licenseViewModel.retry()
        }

        help.setOnClickListener {
            showDoNotAllowScreen()
        }
    }

    private fun showDoNotAllowScreen() {
        val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
        intent.putExtra("source", "license_failed")
        startActivity(intent)
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
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun newInstance(): License {
            return License()
        }
    }
}
