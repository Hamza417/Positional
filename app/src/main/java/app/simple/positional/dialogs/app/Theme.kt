package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.R
import app.simple.positional.theme.setTheme
import app.simple.positional.ui.AppSettings
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_app_theme.*
import java.lang.ref.WeakReference

class Theme(private val weakReference: WeakReference<AppSettings>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_app_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(AppCompatDelegate.getDefaultNightMode())

        light.setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_NO)
            setButtons(AppCompatDelegate.MODE_NIGHT_NO)
        }

        dark.setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_YES)
            setButtons(AppCompatDelegate.MODE_NIGHT_YES)
        }

        follow_system.setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            setButtons(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setButtons(value: Int) {
        light.isChecked = value == AppCompatDelegate.MODE_NIGHT_NO
        dark.isChecked = value == AppCompatDelegate.MODE_NIGHT_YES
        follow_system.isChecked = value == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        weakReference.get()?.setCurrentThemeValue(value)
    }
}