package app.simple.positional.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.dialogs.app.PermissionDialog
import app.simple.positional.firebase.MessagingService
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.services.LocationService
import app.simple.positional.ui.*
import app.simple.positional.util.displayLocationSettingsRequest
import app.simple.positional.util.getLocationStatus
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PermissionCallbacks, BottomSheetSlide {

    private var locationIntent: Intent? = null
    private var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MainPreferences().isScreenOn(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContentView(R.layout.activity_main)

        window.setFormat(PixelFormat.RGBA_8888)

        locationIntent = Intent(applicationContext, LocationService::class.java)

        startService(Intent(applicationContext, MessagingService::class.java))

        runApp()
        checkRunTimePermission()

        bottom_bar.setOnItemSelectedListener {
            setFragment(it)
            FragmentPreferences().setCurrentPage(baseContext, it)
        }

        showReviewPromptToUser()
    }

    private fun showReviewPromptToUser() {

        if (MainPreferences().getLaunchCount(this) < 5) {
            MainPreferences().setLaunchCount(this, MainPreferences().getLaunchCount(this) + 1)
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

    override fun onPause() {
        super.onPause()
        stopService()
    }

    private fun checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (MainPreferences().getShowPermissionDialog(this)) {
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DEFAULT_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPrompt()
                runService()
                baseContext.startService(Intent(this, LocationService::class.java))
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Some features may not work without location permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun runApp() {
        setFragment(FragmentPreferences().getCurrentPage(this))
        bottom_bar.itemActiveIndex = FragmentPreferences().getCurrentPage(this)

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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), DEFAULT_PERMISSION_REQUEST_CODE)
    }

    private fun setFragment(position: Int) {
        when (position) {
            0 -> {
                val clock = supportFragmentManager.findFragmentByTag("clock") as Clock?

                if (clock == null) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, Clock(), "clock")
                            .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, clock, "clock")
                            .commit()
                }
            }
            1 -> {
                val compass = supportFragmentManager.findFragmentByTag("compass") as Compass?

                if (compass == null) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, Compass(), "compass")
                            .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, compass, "compass")
                            .commit()
                }
            }
            2 -> {
                val gps = supportFragmentManager.findFragmentByTag("gps") as GPS?

                if (gps == null) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, GPS(), "gps")
                            .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, gps, "gps")
                            .commit()
                }
            }
            3 -> {
                val level = supportFragmentManager.findFragmentByTag("level") as Level?

                if (level == null) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, Level(), "level")
                            .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, level, "level")
                            .commit()
                }
            }
            4 -> {
                val settings = supportFragmentManager.findFragmentByTag("settings") as AppSettings?

                if (settings == null) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, AppSettings(), "settings")
                            .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.containers, settings, "settings")
                            .commit()
                }
            }
        }
    }

    override fun onBottomSheetSliding(slideOffset: Float) {
        findViewById<FrameLayout>(R.id.bottom_bar_wrapper).translationY = (findViewById<FrameLayout>(R.id.bottom_bar_wrapper).height * slideOffset)
    }
}