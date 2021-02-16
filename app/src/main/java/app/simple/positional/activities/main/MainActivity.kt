package app.simple.positional.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.corners.DynamicCornerFrameLayout
import app.simple.positional.dialogs.app.PermissionDialog
import app.simple.positional.firebase.MessagingService
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.services.LocationService
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.smoothbottombar.SmoothBottomBar
import app.simple.positional.ui.*
import app.simple.positional.util.LocaleHelper
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.LocationPrompt.displayLocationSettingsRequest
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : BaseActivity(), PermissionCallbacks, BottomSheetSlide {

    private val defaultPermissionRequestCode = 123
    private var reviewInfo: ReviewInfo? = null
    private lateinit var bottomBar: SmoothBottomBar
    private lateinit var bottomBarWrapper: DynamicCornerFrameLayout
    private val fragmentTags = arrayOf("clock", "compass", "gps", "level", "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFormat(PixelFormat.RGBA_8888)

        try {
            SharedPreferences.getSharedPreferences()
        } catch (e: UninitializedPropertyAccessException) {
            SharedPreferences.init(applicationContext)
        } catch (e: NullPointerException) {
            SharedPreferences.init(applicationContext)
        }

        if (MainPreferences.isScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        LocaleHelper.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0])

        bottomBar = findViewById(R.id.bottom_bar)
        bottomBarWrapper = findViewById(R.id.bottom_bar_wrapper)

        if (MainPreferences.isNotificationOn()) {
            FirebaseMessaging.getInstance().subscribeToTopic("push_notification")
            startService(Intent(applicationContext, MessagingService::class.java))
        }

        runApp()
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
        runService()
    }

    override fun onPause() {
        super.onPause()
        stopService()
    }

    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (MainPreferences.getShowPermissionDialog()) {
                val permissionDialog = PermissionDialog().newInstance()
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

    private fun runApp() {
        openFragment(FragmentPreferences.getCurrentPage())
        bottomBar.itemActiveIndex = FragmentPreferences.getCurrentPage()
    }

    private fun runService() {
        try {
            applicationContext.startService(Intent(applicationContext, LocationService::class.java))
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun stopService() {
        try {
            applicationContext.stopService(Intent(applicationContext, LocationService::class.java))
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
                        ?: GPS().newInstance()
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
}
