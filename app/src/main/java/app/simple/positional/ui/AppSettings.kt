package app.simple.positional.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.dialogs.app.Issue
import app.simple.positional.dialogs.app.Theme
import app.simple.positional.preference.MainPreferences
import kotlinx.android.synthetic.main.frag_settings.*
import kotlinx.android.synthetic.main.frag_settings.view.*
import java.lang.ref.WeakReference

class AppSettings : Fragment() {

    private lateinit var theme: LinearLayout
    private lateinit var legalNotes: LinearLayout
    private lateinit var fullVersion: LinearLayout

    private lateinit var currentTheme: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        theme = view.findViewById(R.id.settings_theme)
        currentTheme = view.findViewById(R.id.current_theme)
        legalNotes = view.findViewById(R.id.legal_notes)
        fullVersion = view.findViewById(R.id.buy_full)

        if (BuildConfig.FLAVOR == "lite") {
            view.buy_full.visibility = View.VISIBLE
        }

        setCurrentThemeValue(MainPreferences().getCurrentTheme(requireContext()))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentThemeValue(AppCompatDelegate.getDefaultNightMode())

        theme.setOnClickListener {
            val theme = Theme(WeakReference(this))
            theme.show(parentFragmentManager, "null")
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
        currentTheme.text = when (themeValue) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "Follow System"
            4 -> "Day/Night Auto"
            else -> "Unknown Theme Selected!!"
        }
    }
}