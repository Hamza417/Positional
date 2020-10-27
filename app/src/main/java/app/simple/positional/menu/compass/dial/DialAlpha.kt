package app.simple.positional.menu.compass.dial

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu

class DialAlpha {

    var opacity: Float = 0f

    fun dialAlpha(context: Context, compass: Compass) {

        opacity = CompassPreference().getDialOpacity(context)

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Dial Opacity"
                item {
                    label = "25%"
                    icon = if (opacity == 0.25f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.25f
                    }
                }
                item {
                    label = "50%"
                    icon = if (opacity == 0.50f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.50f
                    }
                }
                item {
                    label = "75% / Default"
                    icon = if (opacity == 0.75f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.75f
                    }
                }
                item {
                    label = "100%"
                    icon = if (opacity == 1.0f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 1.0f
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.actionView)

        compassPopupMenu.setOnDismissListener {
            setDialAlpha(opacity, context, compass)
        }
    }

    private fun setDialAlpha(value: Float, context: Context, compass: Compass) {
        CompassPreference().setDialOpacity(value, context)
        compass.setDialAlpha(value)
    }
}