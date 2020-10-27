package app.simple.positional

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.simple.positional.adapters.MenuAdapter
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.dialogs.PermissionDialog
import app.simple.positional.preference.ViewPagerPreference
import app.simple.positional.services.CompassService
import app.simple.positional.services.LocationService
import app.simple.positional.ui.Clock
import app.simple.positional.ui.Compass
import app.simple.positional.ui.GPS
import com.google.android.material.appbar.MaterialToolbar
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle


class MainActivity : AppCompatActivity(), PermissionCallbacks, DuoMenuView.OnMenuClickListener {

    private var locationIntent: Intent? = null
    private var compassIntent: Intent? = null

    private lateinit var drawer: DuoDrawerLayout
    private lateinit var toggle: DuoDrawerToggle
    private lateinit var navMenu: DuoMenuView
    private lateinit var toolbar: MaterialToolbar

    private lateinit var menuAdapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFormat(PixelFormat.RGBA_8888)

        val mTitles = resources.getStringArray(R.array.menuOptions).toList()

        val title: ArrayList<String> = ArrayList()

        for (i in mTitles.indices) {
            title.add(mTitles[i])
            println(mTitles[i])
        }

        locationIntent = Intent(applicationContext, LocationService::class.java)
        compassIntent = Intent(applicationContext, CompassService::class.java)

        toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)
        //toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_drag_handle)
        toolbar.inflateMenu(R.menu.toolbar_items)

        drawer = findViewById(R.id.drawer)
        navMenu = drawer.menuView as DuoMenuView
        menuAdapter = MenuAdapter(title)
        navMenu.adapter = menuAdapter
        navMenu.setOnMenuClickListener(this)
        toggle = DuoDrawerToggle(this, drawer, toolbar, R.string.nav_open, R.string.nav_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        //checkBattery()

        runApp()
        checkRunTimePermission()

        navMenu.setOnClickListener {
            if (drawer.isDrawerOpen) {
                drawer.closeDrawer()
            }
        }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_main_setting) {
                if (ViewPagerPreference().getCurrentPage(baseContext) == 1) {
                    (supportFragmentManager.findFragmentByTag("compass") as Compass?)?.openCompassMenu(findViewById(R.id.menu_main_setting))
                }
            }
            true
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

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            return super.onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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
        setFragment(ViewPagerPreference().getCurrentPage(this))
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

    override fun onFooterClicked() {
        finish()
        println("callled")
    }

    override fun onHeaderClicked() {

    }

    override fun onOptionClicked(position: Int, objectClicked: Any?) {
        Handler().postDelayed({
            setFragment(position)
        }, 200)

        drawer.closeDrawer()
        ViewPagerPreference().setCurrentPage(this, position)
    }

    private fun setFragment(position: Int) {
        menuAdapter.setViewSelected(position, true)
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
                toolbar.setLogo(R.drawable.ic_clock)
                toolbar.title = "  Clock"
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
                toolbar.setLogo(R.drawable.ic_compass)
                toolbar.title = "  Compass"
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
                toolbar.setLogo(R.drawable.ic_calibration)
                toolbar.title = "  GPS"
            }
            3 -> {

            }
            4 -> {

            }
        }
    }
}