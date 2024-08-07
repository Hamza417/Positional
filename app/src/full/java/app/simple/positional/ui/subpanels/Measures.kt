package app.simple.positional.ui.subpanels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.adapters.measure.AdapterMeasures
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.dialogs.measure.MeasureAdd
import app.simple.positional.dialogs.measure.MeasureAdd.Companion.showMeasureAdd
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.model.Measure
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.popups.trail.PopupTrailsMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ViewUtils
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.MeasureViewModel
import com.google.android.material.card.MaterialCardView

class Measures : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var adapterMeasures: AdapterMeasures
    private lateinit var add: MaterialCardView
    private lateinit var art: ImageView

    private val measureViewModel: MeasureViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_measures_list, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        add = view.findViewById(R.id.add)
        art = view.findViewById(R.id.art)

        add.apply {
            radius = MainPreferences.getCornerRadius().toFloat()
            cardElevation = 50F
            ViewUtils.addShadow(this)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        measureViewModel.getMeasureEntries().observe(viewLifecycleOwner) {
            adapterMeasures = AdapterMeasures(it)
            recyclerView.adapter = adapterMeasures

            adapterMeasures.setOnAdapterMeasuresCallbackListener(object : AdapterMeasures.Companion.AdapterMeasuresCallback {
                override fun onMeasureClicked(measure: Measure) {
                    MeasurePreferences.setLastSelectedMeasure(measure.dateCreated)
                }

                override fun onMeasureMenuClicked(measure: Measure, view: View, position: Int) {
                    val popup = PopupTrailsMenu(
                        layoutInflater.inflate(R.layout.popup_trails,
                            DynamicCornerLinearLayout(requireContext())), view)


                    popup.setOnPopupCallbacksListener(object : PopupTrailsMenu.Companion.PopupTrailsCallbacks {
                        override fun onShowOnMap() {
                            MeasurePreferences.setLastSelectedMeasure(measure.dateCreated)
                        }

                        override fun onDelete() {
                            DeletePopupMenu(view) {
                                measureViewModel.deleteMeasure(measure) {
                                    requireActivity().runOnUiThread {
                                        adapterMeasures.deleteMeasure(position).also {
                                            if (adapterMeasures.isEmpty()) {
                                                art.visible(true)
                                            } else {
                                                art.invisible(true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            })

            if (it.isNullOrEmpty()) {
                art.visible(true)
            } else {
                art.invisible(true)
            }
        }

        add.setOnClickListener {
            showMeasureAdd().setOnMeasureAddCallbacks(object :
                MeasureAdd.Companion.MeasureAddCallbacks {
                override fun onSave(name: String, note: String) {
                    measureViewModel.addMeasure(name, note)
                }
            })
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MeasurePreferences.LAST_SELECTED_MEASURE -> {
                if (MeasurePreferences.isMeasureSelected()) {
                    requireActivity().finish()
                }
            }
        }
    }

    companion object {
        fun newInstance(): Measures {
            val args = Bundle()
            val fragment = Measures()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Measures"
    }
}
