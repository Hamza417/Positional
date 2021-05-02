package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomRadioButton
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.ThemeSetter

class Theme : CustomBottomSheetDialogFragment() {

    private lateinit var light: CustomRadioButton
    private lateinit var dark: CustomRadioButton
    private lateinit var followSystem: CustomRadioButton
    private lateinit var dayNight: CustomRadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_theme, container, false)

        light = view.findViewById(R.id.light)
        dark = view.findViewById(R.id.dark)
        followSystem = view.findViewById(R.id.follow_system)
        dayNight = view.findViewById(R.id.day_night)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.isDayNightOn()) {
            setButtons(4)
        } else {
            setButtons(AppCompatDelegate.getDefaultNightMode())
        }

        light.setOnClickListener {
            setButtons(AppCompatDelegate.MODE_NIGHT_NO)
        }

        dark.setOnClickListener {
            setButtons(AppCompatDelegate.MODE_NIGHT_YES)
        }

        followSystem.setOnClickListener {
            setButtons(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        dayNight.setOnClickListener {
            setButtons(4)
        }
    }

    private fun setButtons(value: Int) {

        MainPreferences.setDayNight(value == 4)

        if (value != 4) {
            MainPreferences.setTheme(value = value)
        }

        light.isChecked = value == AppCompatDelegate.MODE_NIGHT_NO
        dark.isChecked = value == AppCompatDelegate.MODE_NIGHT_YES
        followSystem.isChecked = value == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        dayNight.isChecked = value == 4

        ThemeSetter.setAppTheme(value)
    }
}
