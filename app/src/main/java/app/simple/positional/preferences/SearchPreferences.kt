package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

object SearchPreferences {
    private const val lastSearchKeyword = "last_time_zone_search_keyword"

    fun setLastSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(lastSearchKeyword, keyword).apply()
    }

    fun getLastSearchKeyword(): String {
        return getSharedPreferences().getString(lastSearchKeyword, "")!!
    }
}
