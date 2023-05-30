package app.simple.positional.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarAdapter
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.transformers.DepthTransformer
import app.simple.positional.dialogs.app.Permission
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.popups.miscellaneous.PopupFragments
import app.simple.positional.preferences.BottomBarPreferences
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
import app.simple.positional.util.ViewUtils
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class MainActivity : BaseActivity(),
        PermissionCallbacks,
        BottomSheetSlide,
        android.content.SharedPreferences.OnSharedPreferenceChangeListener,
        OnMapsSdkInitializedCallback {

    private val defaultPermissionRequestCode = 123
    private lateinit var bottomBar: ViewPager2
    private lateinit var bottomBarContainer: DynamicCornerLinearLayout
    private lateinit var label: TextView
    private lateinit var bottomBarAdapter: BottomBarAdapter
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(baseContext, MapsInitializer.Renderer.LATEST, this)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bottom_bar)
        bottomBarContainer = findViewById(R.id.bottom_bar_container)
        label = findViewById(R.id.label)

        bottomBarAdapter = BottomBarAdapter(BottomBarItems.getBottomBarItems(baseContext)) {
            PopupFragments(bottomBarContainer) { _, tag, position ->
                bottomBar.setCurrentItem(position, true)
                openFragment(tag)
                FragmentPreferences.setCurrentPage(position)
                FragmentPreferences.setCurrentTag(tag)
            }.setOnDismissListener {
                bottomBarContainer.animate()
                        .alpha(1f)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(500)
                        .start()
            }
        }

        bottomBarAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        ViewUtils.addShadow(bottomBarContainer)

        bottomBar.apply {
            adapter = bottomBarAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(FragmentPreferences.getCurrentPage(), false)
            setPageTransformer(DepthTransformer())

            label.text = BottomBarItems.getBottomBarItems(baseContext)[FragmentPreferences.getCurrentPage()].name
            postDelayed({
                label.visibility = TextView.GONE
            }, 1000)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        val position = bottomBar.currentItem
                        val name = BottomBarItems.getBottomBarItems(baseContext)[position].tag
                        FragmentPreferences.setCurrentPage(position)
                        FragmentPreferences.setCurrentTag(name)
                        openFragment(name)

                        postDelayed({
                            label.visibility = TextView.GONE
                        }, 500)
                    }

                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        label.visibility = TextView.VISIBLE
                        label.alpha = 1f
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    label.text = BottomBarItems.getBottomBarItems(baseContext)[position].name
                }
            })
        }

        checkRunTimePermission()
        MainPreferences.setLaunchCount(MainPreferences.getLaunchCount() + 1)

        if (savedInstanceState.isNull()) {
            openFragment(FragmentPreferences.getCurrentTag())
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
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
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
                Toast.makeText(this, R.string.no_location_permission_alert, Toast.LENGTH_LONG).show()
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
            displayLocationSettingsRequest(this)
        }
    }

    override fun onGrantRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ), defaultPermissionRequestCode)
    }

    private fun openFragment(tag: String) {
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
                return supportFragmentManager.findFragmentByTag("clock") as Time?
                        ?: Time.newInstance()
            }
            "compass" -> {
                return supportFragmentManager.findFragmentByTag("compass") as Compass?
                        ?: Compass.newInstance()
            }
            "direction" -> {
                return supportFragmentManager.findFragmentByTag("direction") as Direction?
                        ?: Direction.newInstance()
            }
            "location" -> {
                return supportFragmentManager.findFragmentByTag("location") as GPS?
                        ?: GPS.newInstance()
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
                return supportFragmentManager.findFragmentByTag("settings") as Settings?
                        ?: Settings.newInstance()
            }
            else -> {
                return supportFragmentManager.findFragmentByTag("location") as GPS?
                        ?: GPS.newInstance()
            }
        }
    }

    override fun onBottomSheetSliding(slideOffset: Float, animate: Boolean) {
        bottomBarContainer.scaleX = 1 - slideOffset
        bottomBarContainer.scaleY = 1 - slideOffset
        bottomBarContainer.alpha = 1 - slideOffset
    }

    override fun onMapClicked(fullScreen: Boolean) {
        if (fullScreen) {
            bottomBarContainer.animate()
                    .scaleX(1F)
                    .scaleY(1F)
                    .alpha(1F)
                    .setInterpolator(DecelerateInterpolator(1.5F)).start()
        } else {
            bottomBarContainer.animate()
                    .scaleX(0F)
                    .scaleY(0F)
                    .alpha(0F)
                    .setInterpolator(DecelerateInterpolator(1.5F)).start()
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
            BottomBarPreferences.clockPanel,
            BottomBarPreferences.compassPanel,
            BottomBarPreferences.gpsPanel,
            BottomBarPreferences.trailPanel,
            BottomBarPreferences.levelPanel,
            BottomBarPreferences.settingsPanel -> {
                bottomBarAdapter.setBottomBarItems(BottomBarItems.getBottomBarItems(applicationContext))
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

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {
        Log.d("Map Initialized", p0.name)
    }
}