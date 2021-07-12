package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object LevelPreferences {

    const val isSquareStyle = "is_square_style"

    //--------------------------------------------------------------------------------------------------//

    fun isSquareStyle(): Boolean {
        return getSharedPreferences().getBoolean(isSquareStyle, false)
    }

    fun setSquareStyle(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSquareStyle, value).apply()
    }
}