package app.simple.positional.dialogs.compass

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import app.simple.positional.R
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preference.CompassPreference

class CompassPhysicalProperties : CustomBottomSheetDialogFragment() {

    private lateinit var dampingCoefficient: SeekBar
    private lateinit var rotationalInertia: SeekBar
    private lateinit var magneticCoefficient: SeekBar
    private lateinit var reset: ImageButton
    private lateinit var help: ImageButton
    private lateinit var toggle: SwitchView
    private lateinit var toggleContainer: LinearLayout

    private var objectAnimator: ObjectAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_physical_properties, container, false)

        dampingCoefficient = view.findViewById(R.id.compass_damping_coefficient_seekbar)
        rotationalInertia = view.findViewById(R.id.compass_rotational_inertia_seekbar)
        magneticCoefficient = view.findViewById(R.id.compass_magnetic_coefficient_seekbar)
        reset = view.findViewById(R.id.reset_physical_properties)
        help = view.findViewById(R.id.help_physical_properties)
        toggle = view.findViewById(R.id.compass_physical_properties)
        toggleContainer = view.findViewById(R.id.compass_toggle_physical_properties)

        dampingCoefficient.max = 50
        rotationalInertia.max = 50
        magneticCoefficient.max = 15000

        dampingCoefficient.progress = CompassPreference.getDampingCoefficient().toInt()
        rotationalInertia.progress = CompassPreference.getRotationalInertia().toInt()
        magneticCoefficient.progress = CompassPreference.getMagneticCoefficient().toInt()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggle.isChecked = CompassPreference.isUsingPhysicalProperties()
        isChecked(toggle.isChecked)

        dampingCoefficient.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                CompassPreference.setDampingCoefficient(if (progress == 0) 0.1F else progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                /* no-op */
            }
        })

        rotationalInertia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                CompassPreference.setRotationalInertia(if (progress == 0) 0.1F else progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                /* no-op */
            }
        })

        magneticCoefficient.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                CompassPreference.setMagneticCoefficient(if (progress == 0) 0.1F else progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                /* no-op */
            }
        })

        reset.setOnClickListener {
            updateSeekbar(dampingCoefficient, 10)
            updateSeekbar(rotationalInertia, 0)
            updateSeekbar(magneticCoefficient, 3000)
        }

        help.setOnClickListener {
            HtmlViewer.newInstance(getString(R.string.physical_properties))
                    .show(childFragmentManager, "help")
        }

        toggleContainer.setOnClickListener {
            toggle.isChecked = !toggle.isChecked
        }

        toggle.setOnCheckedChangeListener {
            CompassPreference.setUsePhysicalProperties(it)
            isChecked(it)
        }
    }

    private fun isChecked(boolean: Boolean) {
        rotationalInertia.isEnabled = !boolean
        dampingCoefficient.isEnabled = !boolean
        magneticCoefficient.isEnabled = !boolean

        rotationalInertia.animate().alpha(if (boolean) 0.5F else 1F).setInterpolator(DecelerateInterpolator(1.5F)).start()
        dampingCoefficient.animate().alpha(if (boolean) 0.5F else 1F).setInterpolator(DecelerateInterpolator(1.5F)).start()
        magneticCoefficient.animate().alpha(if (boolean) 0.5F else 1F).setInterpolator(DecelerateInterpolator(1.5F)).start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        objectAnimator?.cancel()
        dampingCoefficient.clearAnimation()
        rotationalInertia.clearAnimation()
        magneticCoefficient.clearAnimation()
    }

    private fun updateSeekbar(seekBar: SeekBar, value: Int) {
        objectAnimator = ObjectAnimator.ofInt(seekBar, "progress", seekBar.progress, value)
        objectAnimator?.duration = 1000L
        objectAnimator?.interpolator = DecelerateInterpolator()
        objectAnimator?.setAutoCancel(true)
        objectAnimator?.start()
    }
}