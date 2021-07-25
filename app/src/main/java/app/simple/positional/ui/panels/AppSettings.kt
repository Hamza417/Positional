package app.simple.positional.ui.panels

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.activities.subactivity.AccentColorsActivity
import app.simple.positional.activities.subactivity.CustomLocationsActivity
import app.simple.positional.activities.subactivity.WebPageViewerActivity
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.decorations.corners.DynamicCornerFrameLayout
import app.simple.positional.decorations.padding.PaddingAwareNestedScrollView
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.popup.PopupMenuCallback
import app.simple.positional.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.dialogs.app.PanelEditor
import app.simple.positional.dialogs.settings.*
import app.simple.positional.popups.settings.LegalNotesPopupMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.LocaleHelper.localeList
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class AppSettings : ScopedFragment(), CoordinatesCallback, PopupMenuCallback {

    private var xOff = 0F
    private var yOff = 0F

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var buyFull: DynamicRippleLinearLayout
    private lateinit var unit: DynamicRippleLinearLayout
    private lateinit var locationProvider: DynamicRippleLinearLayout
    private lateinit var mapsProvider: DynamicRippleLinearLayout
    private lateinit var language: DynamicRippleLinearLayout
    private lateinit var theme: DynamicRippleLinearLayout
    private lateinit var accent: DynamicRippleLinearLayout
    private lateinit var currentAccent: DynamicCornerFrameLayout
    private lateinit var icon: DynamicRippleTextView
    private lateinit var corner: DynamicRippleTextView
    private lateinit var skipSplashScreenContainer: DynamicRippleConstraintLayout
    private lateinit var customLocation: DynamicRippleConstraintLayout
    private lateinit var panelsEditor: DynamicRippleTextView
    private lateinit var appVersion: DynamicRippleLinearLayout
    private lateinit var legalNotes: DynamicRippleLinearLayout
    private lateinit var github: DynamicRippleLinearLayout
    private lateinit var translate: DynamicRippleLinearLayout
    private lateinit var keepScreenOn: DynamicRippleConstraintLayout

    private lateinit var toggleKeepScreenOn: SwitchView
    private lateinit var toggleCustomLocation: SwitchView
    private lateinit var toggleSkipSplashScreen: SwitchView

    private lateinit var specifiedLocationText: TextView
    private lateinit var currentTheme: TextView
    private lateinit var currentUnit: TextView
    private lateinit var currentLanguage: TextView
    private lateinit var currentLocationProvider: TextView
    private lateinit var currentMapsProvider: TextView
    private lateinit var foundIssues: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        scrollView = view.findViewById(R.id.settings_scroll_view)
        buyFull = view.findViewById(R.id.buy_full)
        unit = view.findViewById(R.id.settings_units)
        locationProvider = view.findViewById(R.id.settings_location_provider)
        mapsProvider = view.findViewById(R.id.settings_map_provider)
        language = view.findViewById(R.id.settings_languages)
        theme = view.findViewById(R.id.settings_theme)
        accent = view.findViewById(R.id.settings_accent)
        currentAccent = view.findViewById(R.id.current_accent)
        icon = view.findViewById(R.id.settings_icons)
        corner = view.findViewById(R.id.settings_corner_radius)
        skipSplashScreenContainer = view.findViewById(R.id.setting_skip_splash_screen_container)
        customLocation = view.findViewById(R.id.setting_custom_location)
        panelsEditor = view.findViewById(R.id.settings_panel_editor)
        appVersion = view.findViewById(R.id.current_app_version)
        legalNotes = view.findViewById(R.id.legal_notes)
        github = view.findViewById(R.id.github)
        foundIssues = view.findViewById(R.id.found_issues)
        translate = view.findViewById(R.id.translate)
        keepScreenOn = view.findViewById(R.id.setting_keep_screen_on)

        toggleKeepScreenOn = view.findViewById(R.id.toggle_screen_on)
        toggleCustomLocation = view.findViewById(R.id.toggle_custom_location)
        toggleSkipSplashScreen = view.findViewById(R.id.toggle_skip_splash_screen)

        specifiedLocationText = view.findViewById(R.id.specified_location_text)
        currentTheme = view.findViewById(R.id.current_theme)
        currentUnit = view.findViewById(R.id.current_unit)
        currentLanguage = view.findViewById(R.id.current_language)
        currentLocationProvider = view.findViewById(R.id.current_location_provider)
        currentMapsProvider = view.findViewById(R.id.current_map_provider)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.isDayNightOn()) {
            setCurrentThemeValue(4)
        } else {
            setCurrentThemeValue(AppCompatDelegate.getDefaultNightMode())
        }

        setCurrentUnit(MainPreferences.getUnit())
        setCurrentLocation()
        setCurrentMapsProvider()
        toggleKeepScreenOn.isChecked = MainPreferences.isScreenOn()
        toggleSkipSplashScreen.isChecked = MainPreferences.getSkipSplashScreen()
        isCoordinatesSet(MainPreferences.isCustomCoordinate())
        currentAccent.backgroundTintList = ColorStateList.valueOf(MainPreferences.getAccentColor())

        for (i in localeList.indices) {
            if (MainPreferences.getAppLanguage() == localeList[i].localeCode) {
                currentLanguage.text = if (i == 0) getString(R.string.auto_system_default_language) else localeList[i].language
            }
        }

        theme.setOnClickListener {
            Theme().show(parentFragmentManager, "null")
        }

        accent.setOnClickListener {
            startActivity(Intent(requireContext(), AccentColorsActivity::class.java))
        }

        icon.setOnClickListener {
            Icons.newInstance().show(childFragmentManager, "app_icons")
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

        mapsProvider.setOnClickListener {
            MapProvider.newInstance()
                    .show(parentFragmentManager, "map_provider")
        }

        language.setOnClickListener {
            Locales().newInstance().show(childFragmentManager, "locales")
        }

        customLocation.setOnClickListener {
            startActivity(Intent(requireActivity(), CustomLocationsActivity::class.java))
        }

        toggleCustomLocation.setOnCheckedChangeListener { isChecked ->
            if (toggleCustomLocation.isPressed) {
                if (isChecked) {
                    startActivity(Intent(requireActivity(), CustomLocationsActivity::class.java))
                } else {
                    MainPreferences.setCustomCoordinates(isChecked)
                }
            }
        }

        panelsEditor.setOnClickListener {
            PanelEditor.newInstance()
                .show(childFragmentManager, "panel_editor")
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
            val popupMenu = LegalNotesPopupMenu(LayoutInflater.from(requireContext()).inflate(R.layout.popup_legal_notes,
                    PopupLinearLayout(context),
                    true), legalNotes, xOff, yOff)
            popupMenu.popupMenuCallback = this
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Positional")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        translate.setOnClickListener {
            val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
            intent.putExtra("source", "translator")
            startActivity(intent)
        }

        foundIssues.setOnClickListener {
            val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
            intent.putExtra("source", "Found Issue")
            startActivity(intent)
        }

        buyFull.setOnClickListener {
            val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
            intent.putExtra("source", "Buy")
            startActivity(intent)
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

    private fun setCurrentMapsProvider() {
        currentMapsProvider.text = if (MainPreferences.getMapPanelType()) {
            "Open Street Maps (alpha)"
        } else {
            "Google Maps"
        }
    }

    override fun isCoordinatesSet(boolean: Boolean) {
        try {
            toggleCustomLocation.isChecked = boolean
        } catch (ignored: NullPointerException) {
        } catch (ignored: UninitializedPropertyAccessException) {
        }
    }

    override fun onMenuItemClicked(source: String) {
        val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
        intent.putExtra("source", source)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        toggleCustomLocation.isChecked = MainPreferences.isCustomCoordinate()
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
            MainPreferences.accentColor -> {
                requireActivity().recreate()
            }
            MainPreferences.isOSMPanel -> {
                setCurrentMapsProvider()
            }
            MainPreferences.isCustomCoordinate -> {
                toggleCustomLocation.isChecked = MainPreferences.isCustomCoordinate()
            }
        }
    }

    companion object {
        fun newInstance(): AppSettings {
            val args = Bundle()
            val fragment = AppSettings()
            fragment.arguments = args
            return fragment
        }
    }
}
