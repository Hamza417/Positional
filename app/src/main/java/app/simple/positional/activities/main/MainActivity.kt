package app.simple.positional.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.dialogs.app.PermissionDialog
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.firebase.MessagingService
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.services.LocationService
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.smoothbottombar.SmoothBottomBar
import app.simple.positional.ui.*
import app.simple.positional.util.displayLocationSettingsRequest
import app.simple.positional.util.getLocationStatus
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory

class MainActivity : AppCompatActivity(), PermissionCallbacks, BottomSheetSlide {

    private var locationIntent: Intent? = null
    private var reviewInfo: ReviewInfo? = null
    private lateinit var bottomBar: SmoothBottomBar
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

        bottomBar = findViewById(R.id.bottom_bar)
        locationIntent = Intent(applicationContext, LocationService::class.java)

        if (!BuildConfig.DEBUG) {
            startService(Intent(applicationContext, MessagingService::class.java))
        }

        runApp()
        checkRunTimePermission()

        bottomBar.setOnItemSelectedListener {
            openFragment(it)
            FragmentPreferences.setCurrentPage(it)
        }

        showReviewPromptToUser()
        showPurchaseDialog(MainPreferences.getLaunchCount())
    }

    private fun showReviewPromptToUser() {

        if (MainPreferences.getLaunchCount() < 5) {
            return
        }

        val manager = ReviewManagerFactory.create(this)

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request_ ->
            if (request_.isSuccessful) {
                reviewInfo = request_.result

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

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (MainPreferences.getShowPermissionDialog()) {
                val permissionDialog = PermissionDialog().newInstance()
                permissionDialog.show(supportFragmentManager, "permission_info")
            } else {
                Toast.makeText(this, "Location Permission Denied!", Toast.LENGTH_LONG).show()
            }
        } else {
            showPrompt()
            runService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DEFAULT_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPrompt()
                runService()
                baseContext.startService(Intent(this, LocationService::class.java))
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "Some features may not work without location permission", Toast.LENGTH_LONG).show()
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
            applicationContext.startService(locationIntent)
        } catch (e: IllegalStateException) {
        }
    }

    private fun stopService() {
        try {
            applicationContext.stopService(locationIntent)
        } catch (e: IllegalStateException) {
        }
    }

    private fun showPrompt() {
        if (!getLocationStatus(this)) {
            displayLocationSettingsRequest(this, this)
        }
    }

    companion object {
        var DEFAULT_PERMISSION_REQUEST_CODE = 123
    }

    override fun onGrantRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ), DEFAULT_PERMISSION_REQUEST_CODE)
    }

    private fun openFragment(position: Int) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                .replace(R.id.containers, getFragment(position), fragmentTags[position])
                .commit()
    }

    private fun getFragment(position: Int): Fragment {
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
            else -> {
                return supportFragmentManager.findFragmentByTag("clock") as Clock?
                        ?: Clock().newInstance()
            }
        }
    }

    override fun onBottomSheetSliding(slideOffset: Float) {
        findViewById<FrameLayout>(R.id.bottom_bar_wrapper).translationY = (findViewById<FrameLayout>(R.id.bottom_bar_wrapper).height * slideOffset)
    }

    private fun showPurchaseDialog(value: Int) {
        if (BuildConfig.FLAVOR == "lite") {
            if (value == 5 || value == 10 || value == 15 || value == 20) {
                HtmlViewer().newInstance("Buy").show(supportFragmentManager, "buy")
            }
        }

        MainPreferences.setLaunchCount(MainPreferences.getLaunchCount() + 1)
    }
}