package app.simple.positional.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.dialogs.app.*
import app.simple.positional.preference.MainPreferences
import kotlinx.android.synthetic.main.frag_settings.*
import kotlinx.android.synthetic.main.frag_settings.view.*
import java.lang.ref.WeakReference

class AppSettings : Fragment(), CoordinatesCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        if (BuildConfig.FLAVOR == "lite") {
            view.buy_full.visibility = View.VISIBLE
            view.specified_location_text.setTextColor(Color.GRAY)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences().isDayNightOn(requireContext())) {
            setCurrentThemeValue(4)
        } else {
            setCurrentThemeValue(AppCompatDelegate.getDefaultNightMode())
        }

        setCurrentUnit(MainPreferences().getUnit(requireContext()))

        toggle_notifications.isChecked = MainPreferences().isNotificationOn(requireContext())
        toggle_screen_on.isChecked = MainPreferences().isScreenOn(requireContext())
        isCoordinatesSet(MainPreferences().isCustomCoordinate(requireContext())) // toggle coordinate switch

        settings_theme.setOnClickListener {
            val theme = Theme(WeakReference(this))
            theme.show(parentFragmentManager, "null")
        }

        settings_units.setOnClickListener {
            val units = WeakReference(Units(WeakReference(this)))
            units.get()?.show(parentFragmentManager, "null")
        }

        setting_custom_location.setOnClickListener {
            if (BuildConfig.FLAVOR == "full") {
                toggle_custom_location.isChecked = true
                val coordinates = Coordinates().newInstance()
                coordinates.coordinatesCallback = this
                coordinates.show(childFragmentManager, "coordinates")
            } else {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
            }
        }

        toggle_custom_location.setOnCheckedChangeListener { _, isChecked ->
            if (BuildConfig.FLAVOR == "full") {
                if (toggle_custom_location.isPressed) {
                    if (isChecked) {
                        val coordinates = Coordinates().newInstance()
                        coordinates.coordinatesCallback = this
                        coordinates.show(childFragmentManager, "coordinates")
                    } else {
                        MainPreferences().setCustomCoordinates(requireContext(), isChecked)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
                toggle_custom_location.isChecked = false
            }
        }

        toggle_screen_on.setOnCheckedChangeListener { _, isChecked ->
            if (toggle_screen_on.isPressed) {
                MainPreferences().setScreenOn(requireContext(), isChecked)

                if (isChecked) {
                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        setting_notification.setOnClickListener {
            toggle_notifications.isChecked = !toggle_notifications.isChecked
        }

        toggle_notifications.setOnCheckedChangeListener { _, isChecked ->
            MainPreferences().setNotifications(requireContext(), isChecked)
        }

        legal_notes.setOnClickListener {
            val popup = PopupMenu(requireContext(), legal_notes)
            popup.menuInflater.inflate(R.menu.legal_notes, popup.menu)
            popup.gravity = Gravity.END

            popup.setOnMenuItemClickListener { item ->
                val legalNotes = LegalNotes().newInstance(item.title.toString())
                legalNotes.show(childFragmentManager, "legal_notes")
                true
            }

            popup.show()

        }

        change_logs.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional/releases")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        found_issues.setOnClickListener {
            val issue = Issue().newInstance()
            issue.show(childFragmentManager, "null")
        }

        buy_full.setOnClickListener {
            val buyFull = BuyFull().newInstance()
            buyFull.show(childFragmentManager, "buy_full")
        }
    }

    fun setCurrentThemeValue(themeValue: Int) {
        try {
            current_theme.text = when (themeValue) {
                AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "Follow System"
                4 -> "Auto (Day/Night)"
                else -> "Unknown Theme Selected!!"
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun setCurrentUnit(value: Boolean) {
        current_unit.text = if (value) "Metric" else "Imperial"
    }

    override fun isCoordinatesSet(boolean: Boolean) {
        try {
            toggle_custom_location.isChecked = boolean
        } catch (e: java.lang.NullPointerException) {
        } catch (e: UninitializedPropertyAccessException) {
        }
    }
}