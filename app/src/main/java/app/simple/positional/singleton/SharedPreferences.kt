package app.simple.positional.singleton

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {

    private const val preferences = "Preferences"
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
    }

    /**
     * Singleton to hold reference of SharedPreference.
     * Call [init] first before making a instance request
     *
     * @see init
     * @throws NullPointerException if the [init] is not called
     * prior to accessing the [sharedPreferences] instance
     */
    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: throw NullPointerException()
    }
}