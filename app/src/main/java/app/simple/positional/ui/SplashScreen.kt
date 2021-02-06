package app.simple.positional.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import app.simple.positional.preference.FragmentPreferences.setCurrentPage
import app.simple.positional.util.BitmapGradient.addLinearGradient
import app.simple.positional.util.BitmapGradient.addRadialGradient
import app.simple.positional.util.getBitmapFromVectorDrawable

class SplashScreen : Fragment() {

    fun newInstance(): SplashScreen {
        return SplashScreen()
    }

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
        val view = inflater.inflate(R.layout.frag_launcher, container, false)

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

        setShortcutScreen()

        if (BuildConfig.FLAVOR == "full") {
            randomDayValue = (LauncherBackground.vectorBackground.indices).random()
            randomNightValue = (LauncherBackground.vectorBackgroundNight.indices).random()
        } else {
            randomDayValue = 5
            randomNightValue = 0
        }

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                colorOne = LauncherBackground.vectorNightColors[randomNightValue][0]
                colorTwo = LauncherBackground.vectorNightColors[randomNightValue][1]
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    launcherBackground.setImageResource(LauncherBackground.vectorBackgroundNight[randomNightValue])
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                colorOne = LauncherBackground.vectorColors[randomDayValue][0]
                colorTwo = LauncherBackground.vectorColors[randomDayValue][1]
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    launcherBackground.setImageResource(LauncherBackground.vectorBackground[randomDayValue])
                }
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }

        touchIndicator.setImageBitmap(R.drawable.ic_touch_indicator.getBitmapFromVectorDrawable(context = requireContext(), 400)?.let { addRadialGradient(it, colorTwo) })

        icon.setImageBitmap(R.drawable.ic_place.getBitmapFromVectorDrawable(context = requireContext(), 400)?.let { addLinearGradient(it, intArrayOf(colorOne, colorTwo)) })
        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.launcher_icon))
        text.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_in))

        launcherContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacksAndMessages(null)
                    touchIndicator.x = event.x - (touchIndicator.width / 2)
                    touchIndicator.y = event.y - (touchIndicator.height / 2)
                    touchIndicator.animate().scaleX(1.2f).scaleY(1.2f).alpha(1.0f).setInterpolator(DecelerateInterpolator()).start()
                }
                MotionEvent.ACTION_MOVE -> {
                    touchIndicator.x = event.x - (touchIndicator.width / 2f)
                    touchIndicator.y = event.y - (touchIndicator.height / 2f)
                }
                MotionEvent.ACTION_UP -> {
                    touchIndicator.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).start()
                    runPostDelayed(1000)
                }
            }

            true
        }
    }

    private fun setShortcutScreen() {
        if (requireActivity().intent.action == null) return
        when (requireActivity().intent.action) {
            "open_clock" -> {
                setScreenValue(0)
            }
            "open_compass" -> {
                setScreenValue(1)
            }
            "open_gps" -> {
                setScreenValue(2)
            }
            "open_level" -> {
                setScreenValue(3)
            }
        }
    }

    private fun setScreenValue(value: Int) {
        setCurrentPage(value)
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
}
