package app.simple.positional.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.decorations.corners.DynamicCornerFrameLayout
import app.simple.positional.dialogs.app.PermissionDialogFragment
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.services.FusedLocationService
import app.simple.positional.services.LocationService
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.smoothbottombar.SmoothBottomBar
import app.simple.positional.ui.*
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.LocationPrompt.displayLocationSettingsRequest
import app.simple.positional.util.NullSafety.isNull
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.*

class MainActivity
    : BaseActivity(), PermissionCallbacks, BottomSheetSlide, android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultPermissionRequestCode = 123
    private var reviewInfo: ReviewInfo? = null
    private lateinit var bottomBar: SmoothBottomBar
    private lateinit var bottomBarWrapper: DynamicCornerFrameLayout
    private val fragmentTags = arrayOf("clock", "compass", "gps", "level", "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bottom_bar)
        bottomBarWrapper = findViewById(R.id.bottom_bar_wrapper)

        if (savedInstanceState.isNull()) {
            openFragment(FragmentPreferences.getCurrentPage())
        }
        bottomBar.itemActiveIndex = FragmentPreferences.getCurrentPage()

        checkRunTimePermission()
        showReviewPromptToUser()

        bottomBar.setOnItemSelectedListener {
            openFragment(it)
            FragmentPreferences.setCurrentPage(it)
        }
    }

    private fun showReviewPromptToUser() {
        if (MainPreferences.getLaunchCount() < 5) {
            MainPreferences.setLaunchCount(MainPreferences.getLaunchCount() + 1)
            return
        }

        val manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
                reviewInfo?.let {
                    val flow = manager.launchReviewFlow(this@MainActivity, it)
                    flow.addOnCompleteListener {
                        /* no-op */
                    }
                }
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
                val permissionDialog = PermissionDialogFragment().newInstance()
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

    private fun openFragment(position: Int) {
        getFragment(position)?.let {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.containers, it, fragmentTags[position])
                    .commit()
        }
    }

    private fun getFragment(position: Int): Fragment? {
        when (position) {
            0 -> {
                return supportFragmentManager.findFragmentByTag("clock") as Clock?
                        ?: Clock().newInstance()
            }
            1 -> {
                return supportFragmentManager.findFragmentByTag("compass") as Compass?
                        ?: Compass().newInstance()
            }
            2 -> {
                return supportFragmentManager.findFragmentByTag("gps") as GPS?
                        ?: GPS.newInstance()
            }
            3 -> {
                return supportFragmentManager.findFragmentByTag("level") as Level?
                        ?: Level().newInstance()
            }
            4 -> {
                return supportFragmentManager.findFragmentByTag("settings") as AppSettings?
                        ?: AppSettings().newInstance()
            }
        }

        return null
    }

    override fun onBottomSheetSliding(slideOffset: Float) {
        bottomBarWrapper.translationY = bottomBarWrapper.height * slideOffset
    }

    override fun onMapClicked(fullScreen: Boolean) {
        if (fullScreen) {
            bottomBarWrapper.animate().translationY(0F).setInterpolator(DecelerateInterpolator(1.5F)).start()
        } else {
            bottomBarWrapper.animate().translationY(bottomBarWrapper.height.toFloat()).setInterpolator(DecelerateInterpolator(1.5F)).start()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        try {
            if (key == MainPreferences.locationProvider) {
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
            }
        } catch (e: IllegalStateException) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
