package app.simple.positional.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.popup.MainListPopupMenu
import app.simple.positional.decorations.popup.PopupMenuCallback
import app.simple.positional.dialogs.settings.*
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.LocaleHelper.localeList
import app.simple.switchview.views.SwitchView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging

class AppSettings : Fragment(), CoordinatesCallback, PopupMenuCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    fun newInstance(): AppSettings {
        val args = Bundle()
        val fragment = AppSettings()
        fragment.arguments = args
        return fragment
    }

    private var xOff = 0F
    private var yOff = 0F

    private lateinit var buyFull: LinearLayout
    private lateinit var unit: LinearLayout
    private lateinit var locationProvider: LinearLayout
    private lateinit var language: LinearLayout
    private lateinit var theme: LinearLayout
    private lateinit var icon: LinearLayout
    private lateinit var corner: LinearLayout
    private lateinit var skipSplashScreenContainer: ConstraintLayout
    private lateinit var customLocation: ConstraintLayout
    private lateinit var pushNotification: ConstraintLayout
    private lateinit var appVersion: LinearLayout
    private lateinit var legalNotes: LinearLayout
    private lateinit var developmentStatus: LinearLayout
    private lateinit var changeLogs: LinearLayout
    private lateinit var github: LinearLayout
    private lateinit var translate: LinearLayout
    private lateinit var keepScreenOn: ConstraintLayout

    private lateinit var toggleNotification: SwitchView
    private lateinit var toggleKeepScreenOn: SwitchView
    private lateinit var toggleCustomLocation: SwitchView
    private lateinit var toggleSkipSplashScreen: SwitchView

    private lateinit var specifiedLocationText: TextView
    private lateinit var currentTheme: TextView
    private lateinit var currentUnit: TextView
    private lateinit var currentLanguage: TextView
    private lateinit var currentLocationProvider: TextView
    private lateinit var foundIssues: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_settings, container, false)

        buyFull = view.findViewById(R.id.buy_full)
        unit = view.findViewById(R.id.settings_units)
        locationProvider = view.findViewById(R.id.settings_location_provider)
        language = view.findViewById(R.id.settings_languages)
        theme = view.findViewById(R.id.settings_theme)
        icon = view.findViewById(R.id.settings_icons)
        corner = view.findViewById(R.id.settings_corner_radius)
        skipSplashScreenContainer = view.findViewById(R.id.setting_skip_splash_screen_container)
        customLocation = view.findViewById(R.id.setting_custom_location)
        pushNotification = view.findViewById(R.id.setting_notification)
        appVersion = view.findViewById(R.id.current_app_version)
        legalNotes = view.findViewById(R.id.legal_notes)
        developmentStatus = view.findViewById(R.id.development_status)
        changeLogs = view.findViewById(R.id.change_logs)
        github = view.findViewById(R.id.github)
        foundIssues = view.findViewById(R.id.found_issues)
        translate = view.findViewById(R.id.translate)
        keepScreenOn = view.findViewById(R.id.setting_keep_screen_on)

        toggleNotification = view.findViewById(R.id.toggle_notifications)
        toggleKeepScreenOn = view.findViewById(R.id.toggle_screen_on)
        toggleCustomLocation = view.findViewById(R.id.toggle_custom_location)
        toggleSkipSplashScreen = view.findViewById(R.id.toggle_skip_splash_screen)

        specifiedLocationText = view.findViewById(R.id.specified_location_text)
        currentTheme = view.findViewById(R.id.current_theme)
        currentUnit = view.findViewById(R.id.current_unit)
        currentLanguage = view.findViewById(R.id.current_language)
        currentLocationProvider = view.findViewById(R.id.current_location_provider)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "lite") {
            buyFull.visibility = View.VISIBLE
            specifiedLocationText.setTextColor(Color.GRAY)
            toggleCustomLocation.isCheckable = false
        }

        if (MainPreferences.isDayNightOn()) {
            setCurrentThemeValue(4)
        } else {
            setCurrentThemeValue(AppCompatDelegate.getDefaultNightMode())
        }

        setCurrentUnit(MainPreferences.getUnit())
        setCurrentLocation()
        toggleNotification.isChecked = MainPreferences.isNotificationOn()
        toggleKeepScreenOn.isChecked = MainPreferences.isScreenOn()
        toggleSkipSplashScreen.isChecked = MainPreferences.getSkipSplashScreen()
        isCoordinatesSet(MainPreferences.isCustomCoordinate())

        for (i in localeList.indices) {
            if (MainPreferences.getAppLanguage() == localeList[i].localeCode) {
                currentLanguage.text = if (i == 0) getString(R.string.auto_system_default_language) else localeList[i].language
            }
        }

        theme.setOnClickListener {
            Theme().show(parentFragmentManager, "null")
        }

        icon.setOnClickListener {
            Icons().newInstance().show(childFragmentManager, "app_icons")
        }

        corner.setOnClickListener {
            RoundedCorner.newInstance().show(parentFragmentManager, "rounded_corners")
        }

        unit.setOnClickListener {
            Units().show(parentFragmentManager, "null")
        }

        locationProvider.setOnClickListener {
            LocationProvider.newInstance().show(childFragmentManager, "location_provider")
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
                Toast.makeText(requireContext(), R.string.only_full_version, Toast.LENGTH_SHORT).show()
            }
        }

        toggleCustomLocation.setOnCheckedChangeListener { isChecked ->
            if (BuildConfig.FLAVOR == "full") {
                if (toggleCustomLocation.isPressed) {
                    if (isChecked) {
                        val coordinates = Coordinates().newInstance()
                        coordinates.coordinatesCallback = this
                        coordinates.show(childFragmentManager, "coordinates")
                    } else {
                        MainPreferences.setCustomCoordinates(isChecked)
                    }
                }
            } else {
                Toast.makeText(requireContext(), R.string.only_full_version, Toast.LENGTH_SHORT).show()
            }
        }

        keepScreenOn.setOnClickListener {
            toggleKeepScreenOn.isChecked = !toggleKeepScreenOn.isChecked
        }

        toggleKeepScreenOn.setOnCheckedChangeListener { isChecked ->
            MainPreferences.setScreenOn(isChecked)

            if (isChecked) {
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        pushNotification.setOnClickListener {
            toggleNotification.isChecked = !toggleNotification.isChecked
        }

        toggleNotification.setOnCheckedChangeListener { isChecked ->
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

        legalNotes.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xOff = event.x
                    yOff = event.y
                }
            }

            false
        }

        legalNotes.setOnClickListener {
            val popupMenu = MainListPopupMenu(LayoutInflater.from(requireContext()).inflate(R.layout.menu_legal_notes,
                    DynamicCornerLinearLayout(context, null),
                    true), legalNotes, xOff, yOff)
            popupMenu.popupMenuCallback = this
        }

        developmentStatus.setOnClickListener {
            HtmlViewer.newInstance("Development Status").show(childFragmentManager, "development_status")
        }

        changeLogs.setOnClickListener {
            HtmlViewer.newInstance("Change Logs").show(childFragmentManager, "change_logs")
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        translate.setOnClickListener {
            HtmlViewer.newInstance("translator").show(childFragmentManager, "translator")
        }

        foundIssues.setOnClickListener {
            HtmlViewer.newInstance("Found Issue").show(childFragmentManager, "Found Issue")
        }

        buyFull.setOnClickListener {
            HtmlViewer.newInstance("Buy").show(childFragmentManager, "buy")
        }

        toggleSkipSplashScreen.setOnCheckedChangeListener { isChecked ->
            MainPreferences.setSkipSplashScreen(isChecked)
        }

        skipSplashScreenContainer.setOnClickListener {
            toggleSkipSplashScreen.isChecked = !toggleSkipSplashScreen.isChecked
        }
    }

    private fun setCurrentLocation() {
        currentLocationProvider.text = when (MainPreferences.getLocationProvider()) {
            "android" -> "Android Location Provider"
            "fused" -> "Fused Location Provider"
            else -> ""
        }
    }

    private fun setCurrentThemeValue(themeValue: Int) {
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

    private fun setCurrentUnit(value: Boolean) {
        currentUnit.text = if (value) getString(R.string.unit_metric) else getString(R.string.unit_imperial)
    }

    override fun isCoordinatesSet(boolean: Boolean) {
        try {
            toggleCustomLocation.isChecked = boolean
        } catch (ignored: NullPointerException) {
        } catch (ignored: UninitializedPropertyAccessException) {
        }
    }

    override fun onMenuItemClicked(source: String) {
        val legalNotes = HtmlViewer.newInstance(source)
        legalNotes.show(childFragmentManager, "legal_notes")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.locationProvider -> {
                setCurrentLocation()
            }
            MainPreferences.unit -> {
                setCurrentUnit(MainPreferences.getUnit())
            }
            MainPreferences.theme -> {
                setCurrentThemeValue(MainPreferences.getTheme())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}
