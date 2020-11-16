package app.simple.positional.ui

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.MainActivity
import app.simple.positional.constants.vectorBackground
import app.simple.positional.constants.vectorBackgroundNight
import app.simple.positional.constants.vectorColors
import app.simple.positional.constants.vectorNightColors
import app.simple.positional.util.addLinearGradient
import app.simple.positional.util.addRadialGradient
import app.simple.positional.util.getBitmapFromVectorDrawable
import kotlinx.android.synthetic.main.frag_launcher.*

class Launcher : Fragment() {

    fun newInstance(): Launcher {
        return Launcher()
    }

    private var randomDayValue: Int = 0
    private var randomNightValue: Int = 0
    private var colorOne: Int = 0x000000
    private var colorTwo: Int = 0x000000
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_launcher, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "full") {
            randomDayValue = (vectorBackground.indices).random()
            randomNightValue = (vectorBackgroundNight.indices).random()
        } else {
            randomDayValue = 5
            randomNightValue = 0
        }

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                colorOne = vectorNightColors[randomNightValue][0]
                colorTwo = vectorNightColors[randomNightValue][1]
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    launcher_background.setImageResource(vectorBackgroundNight[randomNightValue])
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                colorOne = vectorColors[randomDayValue][0]
                colorTwo = vectorColors[randomDayValue][1]
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    launcher_background.setImageResource(vectorBackground[randomDayValue])
                }
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }

        touch_indicator.setImageBitmap(R.drawable.ic_touch_indicator.getBitmapFromVectorDrawable(context = requireContext(), 400)?.let { addRadialGradient(it, colorTwo) })

        launcher_icon.setImageBitmap(R.drawable.ic_place.getBitmapFromVectorDrawable(context = requireContext(), 400)?.let { addLinearGradient(it, intArrayOf(colorOne, colorTwo)) })

        launcher_icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.launcher_icon))
        launcher_text.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_in))

        launcher_act.setOnTouchListener { _, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touch_indicator.x = event.x - (touch_indicator.width / 2)
                    touch_indicator.y = event.y - (touch_indicator.height / 2)

                    touch_indicator.visibility = View.VISIBLE

                    touch_indicator.animate().scaleX(1.2f).scaleY(1.2f).alpha(1.0f).setInterpolator(DecelerateInterpolator()).start()

                    handler.removeCallbacksAndMessages(null)
                }
                MotionEvent.ACTION_MOVE -> {

                    println("${touch_indicator.x} : ${event.x}")

                    touch_indicator.x = event.x - (touch_indicator.width / 2f)
                    touch_indicator.y = event.y - (touch_indicator.height / 2f)
                }
                MotionEvent.ACTION_UP -> {
                    touch_indicator.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).start()
                    runPostDelayed(1000)
                }
            }

            true
        }
    }

    private fun runPostDelayed(delay: Long) {
        handler.postDelayed({
            /**
             * [isDestroyed] and [isFinishing] will check if the activity is alive or not
             * It is possible that the app could have been launched by accident and user might want
             * to close it immediately, in such cases leaving the [Handler.postDelayed] in queue will
             * explicitly execute the action in the background even if the activity is closed
             * and this will run the [MainActivity] and we don't want that
             */
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return@postDelayed
            runIntent()
        }, delay)
    }

    private fun runIntent() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onPause() {
        super.onPause()
        launcher_icon.clearAnimation()
        launcher_text.clearAnimation()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        runPostDelayed(2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}