package app.simple.positional.ui.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.main.MainActivity
import app.simple.positional.constants.*
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.util.BitmapHelper.addLinearGradient
import app.simple.positional.util.BitmapHelper.addRadialGradient
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize

class SplashScreen : Fragment() {

    private lateinit var launcherBackground: ImageView
    private lateinit var touchIndicator: ImageView
    private lateinit var icon: ImageView
    private lateinit var text: AppCompatTextView
    private lateinit var launcherContainer: ConstraintLayout

    private var randomDayValue: Int = 0
    private var randomNightValue: Int = 0
    private var colorOne: Int = 0x000000
    private var colorTwo: Int = 0x000000
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_launcher, container, false)

        launcherBackground = view.findViewById(R.id.launcher_background)
        touchIndicator = view.findViewById(R.id.touch_indicator)
        icon = view.findViewById(R.id.launcher_icon)
        text = view.findViewById(R.id.launcher_text)
        launcherContainer = view.findViewById(R.id.launcher_act)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.DEBUG) {
            @Suppress("SetTextI18n")
            if (BuildConfig.FLAVOR == "lite") {
                text.text = "Positional Lite (Debug)"
            } else {
                text.text = "Positional (Debug)"
            }
        }

        randomDayValue = LauncherBackground.vectorBackground.indices.random()
        randomNightValue = LauncherBackground.vectorBackgroundNight.indices.random()

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                colorOne = LauncherBackground.vectorNightColors[randomNightValue][0]
                colorTwo = LauncherBackground.vectorNightColors[randomNightValue][1]
                launcherBackground.setImageResource(LauncherBackground.vectorBackgroundNight[randomNightValue])

                val window: Window = requireActivity().window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                colorOne = LauncherBackground.vectorColors[randomDayValue][0]
                colorTwo = LauncherBackground.vectorColors[randomDayValue][1]
                launcherBackground.setImageResource(LauncherBackground.vectorBackground[randomDayValue])
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                colorOne = LauncherBackground.vectorColors[randomDayValue][0]
                colorTwo = LauncherBackground.vectorColors[randomDayValue][1]
                launcherBackground.setImageResource(LauncherBackground.vectorBackground[randomDayValue])
            }
        }

        touchIndicator.setImageBitmap(R.drawable.ic_touch_indicator
                .toBitmapKeepingSize(context = requireContext(), 6).let {
                    addRadialGradient(it, colorTwo)
                })

        icon.setImageBitmap(LocationPins.locationsPins[GPSPreferences.getPinSkin()]
                .toBitmapKeepingSize(context = requireContext(), 6).let {
                    addLinearGradient(it, intArrayOf(colorOne, colorTwo))
                })

        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.launcher_icon))
        text.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_in))

        launcherContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacksAndMessages(null)
                    touchIndicator.x = event.x - touchIndicator.width / 2
                    touchIndicator.y = event.y - touchIndicator.height / 2
                    touchIndicator.animate().scaleX(1.2f).scaleY(1.2f).alpha(1.0f).setInterpolator(DecelerateInterpolator()).start()
                    //icon.animate().translationX((event.x - DisplayMetrics().widthPixels / 2) / 50F).translationY(event.y / 50F).setInterpolator(DecelerateInterpolator()).start()
                }
                MotionEvent.ACTION_MOVE -> {
                    touchIndicator.x = event.x - touchIndicator.width / 2f
                    touchIndicator.y = event.y - touchIndicator.height / 2f
                    //icon.translationX = (event.x - DisplayMetrics().widthPixels / 2) / 30F
                    //icon.translationY = event.y / 30F
                }
                MotionEvent.ACTION_UP -> {
                    touchIndicator.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).start()
                    //icon.animate().translationX(0F).translationY(0F).setInterpolator(DecelerateInterpolator()).start()
                    runPostDelayed(1000)
                }
            }

            true
        }
    }

    private fun runPostDelayed(delay: Long) {
        handler.postDelayed({
            /*
             * [isActivityFinishing] will check if the activity is alive or not
             * It is possible that the app could have been launched by accident and user might want
             * to close it immediately, in such cases leaving the [Handler.postDelayed] in queue will
             * explicitly execute the action in the background even if the activity is closed
             * and this will run the [MainActivity] and we don't want that.
             *
             * @see Handler.removeCallbacks
             * @see Handler.removeCallbacksAndMessages
             */
            if (requireActivity().isActivityFinishing()) return@postDelayed
            runIntent()
        }, delay)
    }

    private fun runIntent() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.action = requireActivity().intent.action
        startActivity(intent)
        requireActivity().finish()
    }

    private fun Activity.isActivityFinishing(): Boolean {
        return this.isFinishing || this.isDestroyed
    }

    override fun onPause() {
        icon.clearAnimation()
        text.clearAnimation()
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onResume() {
        runPostDelayed(2000)
        super.onResume()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun newInstance(): SplashScreen {
            return SplashScreen()
        }
    }
}
