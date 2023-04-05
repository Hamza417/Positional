package app.simple.positional.extensions.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.AndroidViewModel
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ContextUtils

open class WrappedViewModel(application: Application) : AndroidViewModel(application), OnSharedPreferenceChangeListener {

    init {
        app.simple.positional.singleton.SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    open fun getContext(): Context {
        return ContextUtils.updateLocale(getApplication<Application>().applicationContext, MainPreferences.getAppLanguage()!!)
    }

    /**
     * Unsure about this so I am currently leaving it here,
     * Will study it later
     */
    open fun getString(resourceID: Int): String {
        return getContext().getString(resourceID)
    }

    fun getString(resId: Int, vararg formatArgs: Any?): String {
        return getContext().getString(resId, *formatArgs)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    override fun onCleared() {
        super.onCleared()
        app.simple.positional.singleton.SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}