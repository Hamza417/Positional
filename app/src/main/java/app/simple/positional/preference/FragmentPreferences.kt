package app.simple.positional.preference

import android.content.Context
import app.simple.positional.R
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

object FragmentPreferences {

    private const val currentPage = "current_page"
    private const val currentTag = "current_tag"

    fun setCurrentPage(value: Int) {
        getSharedPreferences().edit().putInt(currentPage, value).apply()
    }

    fun getCurrentPage(): Int {
        return getSharedPreferences().getInt(currentPage, 2)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setCurrentTag(value: String) {
        getSharedPreferences().edit().putString(currentTag, value).apply()
    }

    fun getCurrentTag(context: Context): String {
        return getSharedPreferences().getString(currentTag, context.getString(R.string.gps_location))!!
    }
}