package app.simple.positional.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarAdapter
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.decorations.corners.DynamicCornerRecyclerView
import app.simple.positional.dialogs.app.Permission
import app.simple.positional.dialogs.app.Rate
import app.simple.positional.preferences.FragmentPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.services.FusedLocationService
import app.simple.positional.services.LocationService
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.ui.panels.*
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.LocationPrompt.displayLocationSettingsRequest

class MainActivity : BaseActivity(),
                     PermissionCallbacks,
                     BottomSheetSlide,
                     android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultPermissionRequestCode = 123
    private lateinit var bottomBar: DynamicCornerRecyclerView
    private lateinit var bottomBarAdapter: BottomBarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBarAdapter = BottomBarAdapter(BottomBarItems.getBottomBarItems(baseContext))
        bottomBarAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        bottomBar = findViewById(R.id.bottom_bar)

        bottomBar.apply {
            layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = bottomBarAdapter
            scheduleLayoutAnimation()
        }

        bottomBarAdapter.onItemClicked = { position, name ->
            println("Called")
            openFragment(name, position)
        }

        checkRunTimePermission()
        showReviewPromptToUser()

        if (savedInstanceState.isNull()) {
            openFragment(FragmentPreferences.getCurrentTag(),
                         FragmentPreferences.getCurrentPage())
        }
    }

    private fun showReviewPromptToUser() {
        if (MainPreferences.getLaunchCount() < 3) {
            MainPreferences.setLaunchCount(MainPreferences.getLaunchCount() + 1)
            return
        } else {
            if (MainPreferences.getShowRatingDialog()) {
                Rate().show(supportFragmentManager, "rate")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        runService()
    }

    override fun onPause() {
        super.onPause()
        SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        stopService()
    }

    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (MainPreferences.getShowPermissionDialog()) {
                val permissionDialog = Permission.newInstance()
                permissionDialog.show(supportFragmentManager, "permission_info")
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
            }
        } else {
            showPrompt()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == defaultPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPrompt()
                runService()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, R.string.no_location_permission_alert, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun runService() {
        try {
            when (MainPreferences.getLocationProvider()) {
                "android" -> startService(Intent(applicationContext, LocationService::class.java))
                "fused" -> startService(Intent(applicationContext, FusedLocationService::class.java))
            }
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun stopService() {
        try {
            when (MainPreferences.getLocationProvider()) {
                "android" -> stopService(Intent(applicationContext, LocationService::class.java))
                "fused" -> stopService(Intent(applicationContext, FusedLocationService::class.java))
            }
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun showPrompt() {
        if (!getLocationStatus(this)) {
            displayLocationSettingsRequest(this, this)
        }
    }

    override fun onGrantRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ), defaultPermissionRequestCode)
    }

    private fun openFragment(tag: String, position: Int) {
        bottomBar.smoothScrollToPosition(position)
        getFragment(tag).let {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                .replace(R.id.containers, it, tag)
                .commit()
        }
    }

    private fun getFragment(name: String): Fragment {
        when (name) {
            "clock" -> {
                return supportFragmentManager.findFragmentByTag("clock") as Clock?
                    ?: Clock.newInstance()
            }
            "compass" -> {
                return supportFragmentManager.findFragmentByTag("compass") as Compass?
                    ?: Compass.newInstance()
            }
            "location" -> {
                return if (MainPreferences.getMapPanelType() && BuildConfig.FLAVOR != "lite") {
                    supportFragmentManager.findFragmentByTag("gps") as OSM?
                        ?: OSM.newInstance()
                } else {
                    supportFragmentManager.findFragmentByTag("gps") as GPS?
                        ?: GPS.newInstance()
                }
            }
            "trail" -> {
                return supportFragmentManager.findFragmentByTag("trail") as Trail?
                    ?: Trail.newInstance()
            }
            "level" -> {
                return supportFragmentManager.findFragmentByTag("level") as Level?
                    ?: Level.newInstance()
            }
            "settings" -> {
                return supportFragmentManager.findFragmentByTag("settings") as AppSettings?
                    ?: AppSettings.newInstance()
            }
            else -> {
                return getFragment("location")
            }
        }
    }

    override fun onBottomSheetSliding(slideOffset: Float) {
        bottomBar.translationY = bottomBar.height * slideOffset
    }

    override fun onMapClicked(fullScreen: Boolean) {
        if (fullScreen) {
            bottomBar.animate().translationY(0F).setInterpolator(DecelerateInterpolator(1.5F)).start()
        } else {
            bottomBar.animate().translationY(bottomBar.height.toFloat()).setInterpolator(DecelerateInterpolator(1.5F)).start()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.locationProvider -> {
                try {
                    when (MainPreferences.getLocationProvider()) {
                        "fused" -> {
                            stopService(Intent(applicationContext, LocationService::class.java))
                            startService(Intent(applicationContext, FusedLocationService::class.java))
                        }
                        "android" -> {
                            stopService(Intent(applicationContext, FusedLocationService::class.java))
                            startService(Intent(applicationContext, LocationService::class.java))
                        }
                    }
                } catch (e: IllegalStateException) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", bottomBar.translationY)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.isNotNull()) {
            bottomBar.translationY = savedInstanceState.getFloat("translation")
        }
        super.onRestoreInstanceState(savedInstanceState)
    }
}
