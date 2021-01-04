package app.simple.positional.preference

import android.content.Context
import android.content.SharedPreferences
import app.simple.positional.util.NullSafety.asNotNull

object SharedPreferences {

    private const val preferences = "Preferences"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences.asNotNull() as SharedPreferences
    }
}