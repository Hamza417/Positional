package app.simple.positional.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
import app.simple.positional.callbacks.PanelsCallback
import app.simple.positional.callbacks.PermissionCallbacks
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.transformers.DepthTransformer
import app.simple.positional.dialogs.app.Panels.Companion.showPanelsDialog
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.preferences.BottomBarPreferences
import app.simple.positional.preferences.FragmentPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.services.FusedLocationService
import app.simple.positional.services.LocationService
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.ui.panels.Compass
import app.simple.positional.ui.panels.Direction
import app.simple.positional.ui.panels.GPS
import app.simple.positional.ui.panels.Level
import app.simple.positional.ui.panels.Measure
import app.simple.positional.ui.panels.Settings
import app.simple.positional.ui.panels.Time
import app.simple.positional.ui.panels.Trail
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
    private lateinit var popupContainer: View
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
//            PopupFragments(bottomBarContainer) { _, tag, position ->
//                bottomBar.setCurrentItem(position, true)
//                openFragment(tag)
//                FragmentPreferences.setCurrentPage(position)
//                FragmentPreferences.setCurrentTag(tag)
//            }.setOnDismissListener {
//                bottomBarContainer.animate()
//                        .alpha(1f)
//                        .setInterpolator(DecelerateInterpolator())
//                        .setDuration(500)
//                        .start()
//            }

            supportFragmentManager.showPanelsDialog(PanelsCallback { _, string, position ->
                bottomBar.setCurrentItem(position, true)
                openFragment(string!!)
                FragmentPreferences.setCurrentPage(position)
                FragmentPreferences.setCurrentTag(string)
            })
        }

        label.setOnClickListener {
            // hideBottomBarMenuAndShowPopup()
        }

        bottomBarAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        ViewUtils.addShadow(bottomBarContainer)

        bottomBar.apply {
            adapter = bottomBarAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(FragmentPreferences.getCurrentPage(), false)
            setPageTransformer(DepthTransformer())

            label.text =
                BottomBarItems.getBottomBarItems(baseContext)[FragmentPreferences.getCurrentPage()].name

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

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("MainActivity", "dispatchTouchEvent: ")
                // Check if touch region is inside the bottom bar
                if (ev.rawY < bottomBarContainer.y || ev.rawX < bottomBarContainer.x) {
                    // showBottomBarMenu()
                    Log.d("MainActivity", "dispatchTouchEvent: inside")
                } else {
                    /* no-op */
                }
            }

            MotionEvent.ACTION_UP -> {
                /* no-op */
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Suppress("unused")
    private fun layOutPopupData() {
        popupContainer = LayoutInflater.from(baseContext)
            .inflate(R.layout.popup_fragments, PopupLinearLayout(baseContext), true)

        popupContainer.findViewById<DynamicRippleTextView>(R.id.clock).setOnClickListener {
            bottomBar.setCurrentItem(0, true)
            openFragment(BottomBarItems.CLOCK)
            FragmentPreferences.setCurrentPage(0)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[0].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.compass).setOnClickListener {
            bottomBar.setCurrentItem(1, true)
            openFragment(BottomBarItems.COMPASS)
            FragmentPreferences.setCurrentPage(1)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[1].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.direction).setOnClickListener {
            bottomBar.setCurrentItem(2, true)
            openFragment(BottomBarItems.DIRECTION)
            FragmentPreferences.setCurrentPage(2)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[2].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.location).setOnClickListener {
            bottomBar.setCurrentItem(3, true)
            openFragment(BottomBarItems.LOCATION)
            FragmentPreferences.setCurrentPage(3)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[3].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.trail).setOnClickListener {
            bottomBar.setCurrentItem(4, true)
            openFragment(BottomBarItems.TRAIL)
            FragmentPreferences.setCurrentPage(4)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[4].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.level).setOnClickListener {
            bottomBar.setCurrentItem(5, true)
            openFragment(BottomBarItems.LEVEL)
            FragmentPreferences.setCurrentPage(5)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[5].tag)
            showBottomBarMenu()
        }

        popupContainer.findViewById<DynamicRippleTextView>(R.id.settings).setOnClickListener {
            bottomBar.setCurrentItem(6, true)
            openFragment(BottomBarItems.SETTINGS)
            FragmentPreferences.setCurrentPage(6)
            FragmentPreferences.setCurrentTag(BottomBarItems.getBottomBarItems(baseContext)[6].tag)
            showBottomBarMenu()
        }
    }

    private fun showBottomBarMenu() {
        bottomBar.visibility = ViewPager2.VISIBLE
        if (label.text != BottomBarItems.getBottomBarItems(baseContext)[FragmentPreferences.getCurrentPage()].name) {
            label.visibility = TextView.VISIBLE
        }
        bottomBarContainer.removeView(popupContainer)
    }

    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (MainPreferences.getShowPermissionDialog()) {
                onGrantRequest()
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
            }
        } else {
            showPrompt()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == defaultPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPrompt()
                runService()
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
                Toast.makeText(this, R.string.no_location_permission_alert, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun runService() {
        try {
            when (MainPreferences.getLocationProvider()) {
                "android" -> startService(Intent(applicationContext, LocationService::class.java))
                "fused" -> startService(
                    Intent(
                        applicationContext,
                        FusedLocationService::class.java
                    )
                )
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
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), defaultPermissionRequestCode
        )
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
            BottomBarItems.CLOCK -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.CLOCK) as Time?
                    ?: Time.newInstance()
            }

            BottomBarItems.COMPASS -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.COMPASS) as Compass?
                    ?: Compass.newInstance()
            }

            BottomBarItems.DIRECTION -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.DIRECTION) as Direction?
                    ?: Direction.newInstance()
            }

            BottomBarItems.LOCATION -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.LOCATION) as GPS?
                    ?: GPS.newInstance()
            }

            BottomBarItems.TRAIL -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.TRAIL) as Trail?
                    ?: Trail.newInstance()
            }

            BottomBarItems.MEASURE -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.MEASURE) as Measure?
                    ?: Measure.newInstance()
            }

            BottomBarItems.LEVEL -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.LEVEL) as Level?
                    ?: Level.newInstance()
            }

            BottomBarItems.SETTINGS -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.SETTINGS) as Settings?
                    ?: Settings.newInstance()
            }

            else -> {
                return supportFragmentManager.findFragmentByTag(BottomBarItems.LOCATION) as GPS?
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

    override fun onSharedPreferenceChanged(
        sharedPreferences: android.content.SharedPreferences?,
        key: String?
    ) {
        when (key) {
            MainPreferences.locationProvider -> {
                try {
                    when (MainPreferences.getLocationProvider()) {
                        "fused" -> {
                            stopService(Intent(applicationContext, LocationService::class.java))
                            startService(
                                Intent(
                                    applicationContext,
                                    FusedLocationService::class.java
                                )
                            )
                        }

                        "android" -> {
                            stopService(
                                Intent(
                                    applicationContext,
                                    FusedLocationService::class.java
                                )
                            )
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
                bottomBarAdapter.setBottomBarItems(
                    BottomBarItems.getBottomBarItems(
                        applicationContext
                    )
                )
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
