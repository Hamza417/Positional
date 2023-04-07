package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.MainPreferences

class CoordinatesFormat : CustomBottomSheetDialogFragment() {

    private lateinit var dd: RadioButton
    private lateinit var ddm: RadioButton
    private lateinit var dms: RadioButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_coordinates_format, container, false)

        dd = view.findViewById(R.id.dd_ddd)
        ddm = view.findViewById(R.id.dd_mm_mmm)
        dms = view.findViewById(R.id.dd_mm_ss_sss)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(MainPreferences.getCoordinatesFormat())

        dd.setOnClickListener {
            setButtons(0)
        }

        ddm.setOnClickListener {
            setButtons(1)
        }

        dms.setOnClickListener {
            setButtons(2)
        }
    }

    private fun setButtons(value: Int) {
        MainPreferences.setCoordinatesFormat(value)

        dd.isChecked = value == 0
        ddm.isChecked = value == 1
        dms.isChecked = value == 2
    }

    companion object {
        fun newInstance(): CoordinatesFormat {
            val args = Bundle()
            val fragment = CoordinatesFormat()
            fragment.arguments = args
            return fragment
        }
    }
}