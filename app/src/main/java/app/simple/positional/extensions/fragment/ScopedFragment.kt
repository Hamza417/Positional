package app.simple.positional.extensions.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.WindowManager
import androidx.fragment.app.Fragment
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

open class ScopedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    protected var isLandscapeOrientation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLandscapeOrientation = isLandscape()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscapeOrientation = isLandscape()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    protected fun isLandscape(): Boolean {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display?.rotation
        } else {
            val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }

        return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            false
        } else {
            rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
        }
    }
}
