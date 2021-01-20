package app.simple.positional.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

open class ContextUtils(context: Context) : ContextWrapper(context) {
    companion object {
        /**
         * Android does not have a default method to change app locale
         * at runtime like changing theme. This method at first only
         * applicable before the app starts, second is not solution
         * rather a work around, third uses deprecated methods for
         * older APIs which can cause issues in some phones.
         *
         * @param baseContext is base context
         * @param localeToSwitchTo is the new locale app to be converted
         */
        fun updateLocale(baseContext: Context, localeToSwitchTo: Locale): ContextWrapper {
            var context = baseContext
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                @Suppress("deprecation")
                configuration.locale = localeToSwitchTo
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                @Suppress("deprecation")
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            return ContextUtils(context)
        }
    }
}
