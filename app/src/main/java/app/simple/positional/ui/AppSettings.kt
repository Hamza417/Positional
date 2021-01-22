package app.simple.positional.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.dialogs.settings.*
import app.simple.positional.preference.MainPreferences
import app.simple.positional.preference.MainPreferences.getUnit
import app.simple.positional.preference.MainPreferences.isCustomCoordinate
import app.simple.positional.preference.MainPreferences.isDayNightOn
import app.simple.positional.preference.MainPreferences.isNotificationOn
import app.simple.positional.preference.MainPreferences.isScreenOn
import app.simple.positional.preference.MainPreferences.setCustomCoordinates
import app.simple.positional.preference.MainPreferences.setScreenOn
import app.simple.positional.util.LocaleHelper.localeList
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.ref.WeakReference

class AppSettings : Fragment(), CoordinatesCallback {

    fun newInstance(): AppSettings {
        val args = Bundle()
        val fragment = AppSettings()
        fragment.arguments = args
        return fragment
    }

    private lateinit var buyFull: LinearLayout
    private lateinit var unit: LinearLayout
    private lateinit var language: LinearLayout
    private lateinit var theme: LinearLayout
    private lateinit var icon: LinearLayout
    private lateinit var customLocation: LinearLayout
    private lateinit var pushNotification: LinearLayout
    private lateinit var appVersion: LinearLayout
    private lateinit var legalNotes: LinearLayout
    private lateinit var changeLogs: LinearLayout
    private lateinit var github: LinearLayout
    private lateinit var foundIssues: LinearLayout
    private lateinit var translate: LinearLayout
    private lateinit var keepScreenOn: LinearLayout

    private lateinit var toggleNotification: SwitchCompat
    private lateinit var toggleKeepScreenOn: SwitchCompat
    private lateinit var toggleCustomLocation: SwitchCompat

    private lateinit var specifiedLocationText: TextView
    private lateinit var currentTheme: TextView
    private lateinit var currentUnit: TextView
    private lateinit var currentLanguage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        buyFull = view.findViewById(R.id.buy_full)
        unit = view.findViewById(R.id.settings_units)
        language = view.findViewById(R.id.settings_languages)
        theme = view.findViewById(R.id.settings_theme)
        icon = view.findViewById(R.id.settings_icons)
        customLocation = view.findViewById(R.id.setting_custom_location)
        pushNotification = view.findViewById(R.id.setting_notification)
        appVersion = view.findViewById(R.id.current_app_version)
        legalNotes = view.findViewById(R.id.legal_notes)
        changeLogs = view.findViewById(R.id.change_logs)
        github = view.findViewById(R.id.github)
        foundIssues = view.findViewById(R.id.found_issues)
        translate = view.findViewById(R.id.translate)
        keepScreenOn = view.findViewById(R.id.setting_keep_screen_on)

        toggleNotification = view.findViewById(R.id.toggle_notifications)
        toggleKeepScreenOn = view.findViewById(R.id.toggle_screen_on)
        toggleCustomLocation = view.findViewById(R.id.toggle_custom_location)

        specifiedLocationText = view.findViewById(R.id.specified_location_text)
        currentTheme = view.findViewById(R.id.current_theme)
        currentUnit = view.findViewById(R.id.current_unit)
        currentLanguage = view.findViewById(R.id.current_language)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "lite") {
            buyFull.visibility = View.VISIBLE
            specifiedLocationText.setTextColor(Color.GRAY)
        }

        if (isDayNightOn()) {
            setCurrentThemeValue(4)
        } else {
            setCurrentThemeValue(AppCompatDelegate.getDefaultNightMode())
        }

        setCurrentUnit(getUnit())

        toggleNotification.isChecked = isNotificationOn()
        toggleKeepScreenOn.isChecked = isScreenOn()
        isCoordinatesSet(isCustomCoordinate()) // toggle coordinate switch

        for (i in localeList.indices) {
            if (MainPreferences.getAppLanguage() == localeList[i].localeCode) {
                currentLanguage.text = localeList[i].language
            }
        }

        theme.setOnClickListener {
            val theme = Theme(WeakReference(this))
            theme.show(parentFragmentManager, "null")
        }

        icon.setOnClickListener {
            Icons().newInstance().show(childFragmentManager, "app_icons")
        }

        unit.setOnClickListener {
            val units = WeakReference(Units(WeakReference(this)))
            units.get()?.show(parentFragmentManager, "null")
        }

        language.setOnClickListener {
            Locales().newInstance().show(childFragmentManager, "locales")
        }

        customLocation.setOnClickListener {
            if (BuildConfig.FLAVOR == "full") {
                toggleCustomLocation.isChecked = true
                val coordinates = Coordinates().newInstance()
                coordinates.coordinatesCallback = this
                coordinates.show(childFragmentManager, "coordinates")
            } else {
                buyFull.callOnClick()
            }
        }

        toggleCustomLocation.setOnCheckedChangeListener { _, isChecked ->
            if (BuildConfig.FLAVOR == "full") {
                if (toggleCustomLocation.isPressed) {
                    if (isChecked) {
                        val coordinates = Coordinates().newInstance()
                        coordinates.coordinatesCallback = this
                        coordinates.show(childFragmentManager, "coordinates")
                    } else {
                        setCustomCoordinates(isChecked)
                    }
                }
            } else {
                Toast.makeText(requireContext(), R.string.only_full_version, Toast.LENGTH_SHORT).show()
                toggleCustomLocation.isChecked = false
            }
        }

        keepScreenOn.setOnClickListener {
            toggleKeepScreenOn.isChecked = !toggleKeepScreenOn.isChecked
        }

        toggleKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            setScreenOn(isChecked)

            if (isChecked) {
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        pushNotification.setOnClickListener {
            toggleNotification.isChecked = !toggleNotification.isChecked
        }

        toggleNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic("push_notification")
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("push_notification")
            }

            MainPreferences.setNotifications(isChecked)
        }

        appVersion.setOnClickListener {
            val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, requireActivity(), 1337)
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    Toast.makeText(requireContext(), R.string.no_update, Toast.LENGTH_SHORT).show()
                }
            }

            appUpdateInfoTask.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }

        legalNotes.setOnClickListener {
            val popup = PopupMenu(requireContext(), legalNotes)
            popup.menuInflater.inflate(R.menu.legal_notes, popup.menu)
            popup.gravity = Gravity.END

            popup.setOnMenuItemClickListener { item ->
                val legalNotes = HtmlViewer().newInstance(item.title.toString())
                legalNotes.show(childFragmentManager, "legal_notes")
                true
            }

            popup.show()
        }

        changeLogs.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional/releases")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        translate.setOnClickListener {
            HtmlViewer().newInstance("translator").show(childFragmentManager, "translator")
        }

        foundIssues.setOnClickListener {
            HtmlViewer().newInstance("Found Issue").show(childFragmentManager, "Found Issue")
        }

        buyFull.setOnClickListener {
            HtmlViewer().newInstance("Buy").show(childFragmentManager, "buy")
        }
    }

    fun setCurrentThemeValue(themeValue: Int) {
        try {
            currentTheme.text = when (themeValue) {
                AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.theme_day)
                AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.theme_night)
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> getString(R.string.theme_follow_system)
                4 -> getString(R.string.theme_auto)
                else -> "Unknown Theme Selected!!" // Unreachable
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun setCurrentUnit(value: Boolean) {
        currentUnit.text = if (value) getString(R.string.unit_metric) else getString(R.string.unit_imperial)
    }

    override fun isCoordinatesSet(boolean: Boolean) {
        try {
            toggleCustomLocation.isChecked = boolean
        } catch (e: java.lang.NullPointerException) {
        } catch (e: UninitializedPropertyAccessException) {
        }
    }
}
