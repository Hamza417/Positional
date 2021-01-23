package app.simple.positional.activities.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.util.ContextUtils

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        SharedPreferences.init(newBase)
        super.attachBaseContext(ContextUtils.updateLocale(newBase, MainPreferences.getAppLanguage()!!))
    }
}
