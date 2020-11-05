package app.simple.positional.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.preference.MainPreferences
import app.simple.positional.theme.setTheme
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_settings.*
import kotlinx.android.synthetic.main.frag_settings.view.*


class AppSettings : Fragment() {

    private lateinit var theme: LinearLayout
    private lateinit var accent: LinearLayout
    private lateinit var legalNotes: LinearLayout
    private lateinit var fullVersion: LinearLayout

    private lateinit var currentTheme: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        theme = view.findViewById(R.id.settings_theme)
        accent = view.findViewById(R.id.settings_accent)
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

        theme.setOnClickListener {

            var themeValue = MainPreferences().getCurrentTheme(requireContext())
            val popupMenu = popupMenu {
                style = R.style.popupMenu
                dropdownGravity = Gravity.CENTER
                dropDownHorizontalOffset = theme.width / 4
                dropDownVerticalOffset = theme.height / 2 // this@AppSettings.view?.findViewById<CoordinatorLayout>(R.id.settings_container)?.height?.div(4)
                fixedContentWidthInPx = 500
                section {
                    title = "Theme"
                    item {
                        label = "Light"
                        icon = if (themeValue == 1) {
                            R.drawable.ic_radio_button_checked
                        } else R.drawable.ic_radio_button_unchecked
                        callback = {
                            themeValue = 1
                        }
                    }
                    item {
                        label = "Dark"
                        icon = if (themeValue == 2) {
                            R.drawable.ic_radio_button_checked
                        } else R.drawable.ic_radio_button_unchecked
                        callback = {
                            themeValue = 2
                        }
                    }
                    item {
                        label = "Follow System"
                        icon = if (themeValue == 3) {
                            R.drawable.ic_radio_button_checked
                        } else R.drawable.ic_radio_button_unchecked
                        callback = {
                            themeValue = 3
                        }
                    }
                }
            }

            popupMenu.show(context = requireContext(), anchor = theme)

            popupMenu.setOnDismissListener {
                MainPreferences().setCurrentTheme(requireContext(), themeValue)

                //ThemeManager.instance?.applyTheme(requireActivity())

                setTheme(themeValue)
                setCurrentThemeValue(themeValue)
            }
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        found_issues.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional/issues/new")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
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
                        Uri.parse("http://play.google.com/store/apps/details?id=app.simple.positional")))
            }
        }
    }

    private fun setCurrentThemeValue(themeValue: Int) {
        currentTheme.text = when (themeValue) {
            1 -> "Light"
            2 -> "Dark"
            3 -> "Follow System"
            4 -> "Day/Night Auto"
            else -> "Unknown Theme Selected!!"
        }
    }
}