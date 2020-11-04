package app.simple.positional.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.dialogs.PermissionDialog
import app.simple.positional.preference.FragmentPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.services.CompassService
import app.simple.positional.services.LocationService
import app.simple.positional.smoothbottombar.SmoothBottomBar
import app.simple.positional.ui.AppSettings
import app.simple.positional.ui.Clock
import app.simple.positional.ui.Compass
import app.simple.positional.ui.GPS

class MainActivity : AppCompatActivity(), PermissionCallbacks, BottomSheetSlide {

    private var locationIntent: Intent? = null
    private var compassIntent: Intent? = null

    private lateinit var bottomBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This will keep the screen on during test
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        app.simple.positional.theme.setTheme(MainPreferences().getCurrentTheme(baseContext))

        setContentView(R.layout.activity_main)

        window.setFormat(PixelFormat.RGBA_8888)

        windowManager.defaultDisplay.refreshRate

        bottomBar = findViewById(R.id.bottom_bar)
        locationIntent = Intent(applicationContext, LocationService::class.java)
        compassIntent = Intent(applicationContext, CompassService::class.java)

        //checkBattery()

        runApp()
        checkRunTimePermission()

        bottomBar.setOnItemSelectedListener {
            setFragment(it)
            FragmentPreferences().setCurrentPage(baseContext, it)
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
                val permissionDialog = PermissionDialog(this)
                permissionDialog.show(supportFragmentManager, "permission_info")
            } else {
                runService()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DEFAULT_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runService()
                baseContext.startService(Intent(this, LocationService::class.java))
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((applicationContext as Activity), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    finish()
                }
            }
        }
    }

    private fun runApp() {
        setFragment(FragmentPreferences().getCurrentPage(this))
        bottomBar.itemActiveIndex = FragmentPreferences().getCurrentPage(this)
    }

    private fun runService() {
        applicationContext.startService(locationIntent)
        applicationContext.startService(compassIntent)
    }

    private fun stopService() {
        applicationContext.stopService(locationIntent)
        applicationContext.stopService(compassIntent)
    }

    companion object {
        var DEFAULT_PERMISSION_REQUEST_CODE = 123
    }

    /**
     * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
     */
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = baseContext.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = baseContext.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    private fun checkBattery() {
        if (!isIgnoringBatteryOptimizations() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val name = resources.getString(R.string.app_name)
            Toast.makeText(applicationContext, "Battery optimization -> All apps -> $name -> Don't optimize", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
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
            4 -> {

            }
        }
    }

    override fun onBottomSheetSliding(slideOffset: Float) {
        //bottomBar.translationY = (bottomBar.height * slideOffset)
        findViewById<FrameLayout>(R.id.bottom_bar_wrapper).translationY = (findViewById<FrameLayout>(R.id.bottom_bar_wrapper).height * slideOffset)
    }
}