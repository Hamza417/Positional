package app.simple.positional.popups.miscellaneous

import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import kotlin.math.hypot


class PopupFragments(view: View, val callbacks: (View, String, Int) -> Unit) : BasePopupWindow() {

    private var time: DynamicRippleTextView
    private var compass: DynamicRippleTextView
    private var direction: DynamicRippleTextView
    private var gps: DynamicRippleTextView
    private var trail: DynamicRippleTextView
    private var level: DynamicRippleTextView
    private var settings: DynamicRippleTextView

    init {
        val popupLinearLayout = PopupLinearLayout(view.context)
        val containerView = LayoutInflater.from(view.context).inflate(R.layout.popup_fragments, popupLinearLayout, true)

        time = containerView.findViewById(R.id.clock)
        compass = containerView.findViewById(R.id.compass)
        direction = containerView.findViewById(R.id.direction)
        gps = containerView.findViewById(R.id.location)
        trail = containerView.findViewById(R.id.trail)
        level = containerView.findViewById(R.id.level)
        settings = containerView.findViewById(R.id.settings)

        time.setOnClickListener {
            callbacks(it, BottomBarItems.CLOCK, 0)
            dismiss()
        }

        compass.setOnClickListener {
            callbacks(it, BottomBarItems.COMPASS, 1)
            dismiss()
        }

        direction.setOnClickListener {
            callbacks(it, BottomBarItems.DIRECTION, 2)
            dismiss()
        }

        gps.setOnClickListener {
            callbacks(it, BottomBarItems.LOCATION, 3)
            dismiss()
        }

        trail.setOnClickListener {
            callbacks(it, BottomBarItems.TRAIL, 4)
            dismiss()
        }

        level.setOnClickListener {
            callbacks(it, BottomBarItems.LEVEL, 5)
            dismiss()
        }

        settings.setOnClickListener {
            callbacks(it, BottomBarItems.SETTINGS, 6)
            dismiss()
        }

        popupLinearLayout.post {
            circularReveal(popupLinearLayout)
            view.animate()
                    .alpha(0.5F)
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .setDuration(500)
                    .start()
        }

        init(containerView, view)
    }

    private fun circularReveal(view: View) {
        val centerX = view.width / 2
        val centerY = view.height / 2
        val finalRadius = hypot(centerX.toDouble(), centerY.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(
                view,
                centerX,
                centerY,
                0f,
                finalRadius
        )
        circularReveal.interpolator = DecelerateInterpolator(2F)
        circularReveal.duration = 500 // Adjust the duration as needed
        view.visibility = View.VISIBLE
        circularReveal.start()
    }
}