package app.simple.positional.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.dialogs.app.Issue
import app.simple.positional.dialogs.app.LegalNotes
import app.simple.positional.dialogs.app.Theme
import app.simple.positional.dialogs.app.Units
import app.simple.positional.preference.MainPreferences
import kotlinx.android.synthetic.main.frag_settings.*
import kotlinx.android.synthetic.main.frag_settings.view.*
import java.lang.ref.WeakReference


class AppSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        if (BuildConfig.FLAVOR == "lite") {
            view.buy_full.visibility = View.VISIBLE
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

        settings_theme.setOnClickListener {
            val theme = Theme(WeakReference(this))
            theme.show(parentFragmentManager, "null")
        }

        settings_units.setOnClickListener {
            val units = WeakReference(Units(WeakReference(this)))
            units.get()?.show(parentFragmentManager, "null")
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
                legalNotes.show(parentFragmentManager, "legal_notes")
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
            issue.show(parentFragmentManager, "null")
        }

        buy_full.setOnClickListener {
            val uri: Uri = Uri.parse("market://details?id=app.simple.positional")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market back stack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=app.simple.positional")))
            }
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
}