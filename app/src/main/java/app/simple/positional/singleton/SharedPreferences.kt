package app.simple.positional.singleton

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {

    private const val preferences = "Preferences"
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: throw UninitializedPropertyAccessException()
    }
}