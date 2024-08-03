package app.simple.positional.ui.panels

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.decorations.measure.MeasureMaps
import app.simple.positional.decorations.measure.MeasureToolbar
import app.simple.positional.decorations.measure.MeasureToolbarCallbacks
import app.simple.positional.decorations.measure.MeasureTools
import app.simple.positional.dialogs.measure.MeasureMenu.Companion.showMeasureMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.viewmodels.viewmodel.LocationViewModel

class Measure : ScopedFragment() {

    private lateinit var toolbar: MeasureToolbar
    private lateinit var tools: MeasureTools

    private var backPress: OnBackPressedDispatcher? = null
    private var maps: MeasureMaps? = null
    private var locationViewModel: LocationViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.tools)
        maps = view.findViewById(R.id.map)

        backPress = requireActivity().onBackPressedDispatcher
        maps?.onCreate(savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setMeasureToolbarCallbacks(object : MeasureToolbarCallbacks {
            override fun onAdd(view: View?) {
                Log.d(TAG, "onAdd: ")
            }

            override fun onMeasures(view: View?) {
                Log.d(TAG, "onMeasures: ")
            }

            override fun onMenu(view: View?) {
                showMeasureMenu()
            }
        })

        locationViewModel?.getLocation()?.observe(viewLifecycleOwner) { location ->
            Log.d(TAG, "onViewCreated: $location")
        }
    }

    override fun onResume() {
        super.onResume()
        maps?.onResume()
    }

    override fun onPause() {
        super.onPause()
        maps?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    companion object {
        fun newInstance(): Measure {
            val args = Bundle()
            val fragment = Measure()
            fragment.arguments = args
            return fragment
        }

        const val TAG = BottomBarItems.MEASURE
    }
}
