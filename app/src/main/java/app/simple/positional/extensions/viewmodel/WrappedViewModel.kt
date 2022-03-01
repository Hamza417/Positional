package app.simple.positional.extensions.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ContextUtils

open class WrappedViewModel(application: Application) : AndroidViewModel(application) {
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
}