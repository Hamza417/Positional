package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

object FragmentPreferences {

    private const val currentPage = "current_page"

    fun setCurrentPage(value: Int) {
        getSharedPreferences().edit().putInt(currentPage, value).apply()
    }

    fun getCurrentPage(): Int {
        return getSharedPreferences().getInt(currentPage, 2)
    }
}