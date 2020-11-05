package app.simple.positional.preference

import android.content.Context
import app.simple.positional.constants.currentPage
import app.simple.positional.constants.preferences

class FragmentPreferences {
    fun setCurrentPage(context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(currentPage, value).apply()
    }

    fun getCurrentPage(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(currentPage, 1)
    }
}