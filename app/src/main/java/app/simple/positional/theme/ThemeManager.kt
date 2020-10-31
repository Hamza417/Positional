package app.simple.positional.theme

import android.R
import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper


class ThemeManager {
    //endregion
    //region Properties
    @get:Nullable
    @Nullable
    var currentTheme: Int? = null

    @get:Nullable
    @Nullable
    var nightMode: Int? = null

    //endregion
    //region Methods
    fun getTypedValueForAttr(context: Context, @AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }

    fun getColorPrimary(context: Context): Int {
        return getTypedValueForAttr(context, R.attr.colorPrimary).data
    }

    fun getColorPrimaryDark(context: Context): Int {
        return getTypedValueForAttr(context, R.attr.colorPrimaryDark).data
    }

    fun getColorAccent(context: Context): Int {
        return getTypedValueForAttr(context, R.attr.colorAccent).data
    }

    fun applyTheme(contextThemeWrapper: ContextThemeWrapper) {
        if (currentTheme != null) contextThemeWrapper.setTheme(currentTheme!!)
        if (nightMode != null) AppCompatDelegate.setDefaultNightMode(nightMode!!)
    } //endregion

    companion object {
        //region Singleton
        @Volatile
        var instance: ThemeManager? = null
            get() {
                var result = field
                if (result == null) {
                    synchronized(mutex) {
                        result = field
                        if (result == null) {
                            result = ThemeManager()
                            field = result
                        }
                    }
                }
                return result
            }
            private set
        private val mutex = Any()
    }
}