package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.preference.LevelPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.views.CustomBottomSheetDialogFragment
import app.simple.positional.views.CustomWebView

class ErrorDialog : CustomBottomSheetDialogFragment() {

    private lateinit var webView: CustomWebView
    private lateinit var closeButton: Button
    private lateinit var showAgainCheckBox: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_error, container, false)

        webView = view.findViewById(R.id.error_webview)
        closeButton = view.findViewById(R.id.close_error_dialog)
        showAgainCheckBox = view.findViewById(R.id.show_again_error_dialog)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        when (requireArguments().getString("source")) {
            "Play Services" -> {
                webView.loadUrl("file:///android_asset/html/error_play_services.html")
            }
            "Compass Sensor" -> {
                webView.loadUrl("file:///android_asset/html/error_compass_sensor.html")
            }
            "Level Sensor" -> {
                webView.loadUrl("file:///android_asset/html/error_level_sensor.html")
            }
        }

        showAgainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            when (requireArguments().getString("source")) {
                "Play Services" -> {
                    MainPreferences.setShowPlayServiceDialog(isChecked)
                }
                "Compass Sensor" -> {
                    CompassPreference.setNoSensorAlert(isChecked)
                }
                "Level Sensor" -> {
                    LevelPreferences.setNoSensorAlert(isChecked)
                }
            }
        }

        closeButton.setOnClickListener {
            this.dismiss()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(source: String): ErrorDialog {
            val args = Bundle()
            args.putString("source", source)
            val fragment = ErrorDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
