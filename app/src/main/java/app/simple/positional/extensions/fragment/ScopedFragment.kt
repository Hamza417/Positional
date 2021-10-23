package app.simple.positional.extensions.fragment

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

open class ScopedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}
}
