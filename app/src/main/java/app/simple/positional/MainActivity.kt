package app.simple.positional

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.simple.positional.services.CompassService
import app.simple.positional.services.LocationService
import app.simple.positional.ui.ViewPagerFragment

class MainActivity : AppCompatActivity() {

    private var locationIntent: Intent? = null
    private var compassIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFormat(PixelFormat.RGBA_8888)
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        //getWindow().setStatusBarColor(AttributeColorKt.resolveAttrColor(getBaseContext(), R.attr.mainBackground));
        window.statusBarColor = Color.parseColor("#f6f6f6")
        window.navigationBarColor = Color.parseColor("#f6f6f6")

        //findViewById(R.id.containers).setPadding(0, StatusAndNavigationBarHeight.getStatusBarHeight(getResources()), 0, StatusAndNavigationBarHeight.getNavigationBarHeight(getResources()));
        locationIntent = Intent(applicationContext, LocationService::class.java)
        compassIntent = Intent(applicationContext, CompassService::class.java)

        //checkBattery()

        runApp()
        checkRunTimePermission()
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
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 10)
            } else {
                baseContext.startService(Intent(this, LocationService::class.java))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runApp()
                baseContext.startService(Intent(this, LocationService::class.java))
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((applicationContext as Activity), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    finish()
                }
            }
        }
    }

    private fun runApp() {
        val viewPagerFragment = supportFragmentManager.findFragmentByTag("model") as ViewPagerFragment?
        if (viewPagerFragment == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.containers, ViewPagerFragment(), "model")
                    .commit()
        } else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.containers, viewPagerFragment, "model")
                    .commit()
        }
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
}