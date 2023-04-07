package app.simple.positional.dialogs.settings

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MainPreferences.getCornerRadius
import app.simple.positional.util.StringUtils.buildSpannableString
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class RoundedCorners : CustomBottomSheetDialogFragment() {

    private lateinit var radiusValue: TextView
    private lateinit var radiusSeekBar: SeekBar
    private lateinit var cancel: DynamicRippleButton
    private lateinit var set: DynamicRippleButton
    private lateinit var cornerFrameLayout: DynamicCornerLinearLayout

    private var objectAnimator: ObjectAnimator? = null
    private var lastCornerValue = 0
    private var isValueSet = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_corner, container, false)

        radiusValue = view.findViewById(R.id.app_corner_radius_textview)
        radiusSeekBar = view.findViewById(R.id.app_corner_radius_seekbar)
        cancel = view.findViewById(R.id.app_corner_radius_cancel)
        set = view.findViewById(R.id.app_corner_radius_set)
        cornerFrameLayout = view.findViewById(R.id.app_corner_dialog_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        lastCornerValue = getCornerRadius() * 5
        radiusValue.text = buildSpannableString("${getCornerRadius()} px", 2)
        radiusSeekBar.max = 400
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            radiusSeekBar.min = 25
        }
        updateSeekbar(getCornerRadius() * 5)

        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress_: Int, fromUser: Boolean) {
                val progress = if (progress_ < 25) 25 else progress_

                radiusValue.text = buildSpannableString("${progress / 5F} px", 2)
                updateBackground(progress / 5F)

                if (fromUser) MainPreferences.setCornerRadius(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                objectAnimator?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        set.setOnClickListener {
            isValueSet = true
            this.dismiss()
        }

        cancel.setOnClickListener {
            isValueSet = false
            this.dismiss()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isValueSet) {
            requireActivity().recreate()
        } else {
            MainPreferences.setCornerRadius(lastCornerValue)
        }
        super.onDismiss(dialog)
    }

    private fun updateBackground(radius: Float) {
        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

        cornerFrameLayout.background = MaterialShapeDrawable(shapeAppearanceModel)
    }

    private fun updateSeekbar(value: Int) {
        objectAnimator = ObjectAnimator.ofInt(radiusSeekBar, "progress", radiusSeekBar.progress, value)
        objectAnimator?.duration = 1000L
        objectAnimator?.interpolator = DecelerateInterpolator(1.5F)
        objectAnimator?.setAutoCancel(true)
        objectAnimator?.start()
    }

    companion object {
        fun newInstance(): RoundedCorners {
            val args = Bundle()
            val fragment = RoundedCorners()
            fragment.arguments = args
            return fragment
        }
    }
}
