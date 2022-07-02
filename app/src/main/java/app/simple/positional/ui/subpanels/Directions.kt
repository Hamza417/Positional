package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.adapters.direction.AdapterDirections
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.dialogs.direction.DirectionTarget
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.model.DirectionModel
import app.simple.positional.popups.directions.PopupDirectionsMenu
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.preferences.DirectionPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.viewmodels.viewmodel.DirectionsViewModel
import com.google.android.material.card.MaterialCardView

class Directions : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var add: MaterialCardView
    private val directionsViewModel: DirectionsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_directions, container, false)

        recyclerView = view.findViewById(R.id.directions_recycler_view)
        add = view.findViewById(R.id.add)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add.radius = MainPreferences.getCornerRadius().toFloat()

        add.setOnClickListener {
            val p0 = DirectionTarget.newInstance()

            p0.setOnDirectionTargetListener(object : DirectionTarget.Companion.DirectionTargetCallbacks {
                override fun onDirectionAdded(directionModel: DirectionModel) {
                    directionsViewModel.addDirection(directionModel)
                }
            })

            p0.show(childFragmentManager, "direction_target")
        }

        directionsViewModel.getDirections().observe(viewLifecycleOwner) {
            val adapter = AdapterDirections(it)

            adapter.setOnDirectionCallbacksListener(object : AdapterDirections.Companion.AdapterDirectionsCallbacks {
                override fun onDirectionClicked(directionModel: DirectionModel) {
                    set(directionModel)
                }

                override fun onDirectionLongPressed(directionModel: DirectionModel) {

                }

                override fun onMenuPressed(directionModel: DirectionModel, view: View) {
                    val popup = PopupDirectionsMenu(view)


                    popup.setOnPopupCallbacksListener(object : PopupDirectionsMenu.Companion.PopupDirectionsCallbacks {
                        override fun onSet() {
                            set(directionModel)
                        }

                        override fun onDelete() {
                            val deletePopupMenu = DeletePopupMenu(view)

                            deletePopupMenu.setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                                override fun delete() {
                                    directionsViewModel.deleteDirection(directionModel)
                                }
                            })
                        }

                        override fun onEdit() {
                            val p0 = DirectionTarget.newInstance(directionModel)

                            p0.setOnDirectionTargetListener(object : DirectionTarget.Companion.DirectionTargetCallbacks {
                                override fun onDirectionAdded(directionModel: DirectionModel) {
                                    directionsViewModel.addDirection(directionModel)
                                }
                            })

                            p0.show(childFragmentManager, "direction_target")
                        }
                    })
                }
            })

            recyclerView.adapter = adapter
        }
    }

    private fun set(directionModel: DirectionModel) {
        DirectionPreferences.setTargetLatitude(directionModel.latitude.toFloat())
        DirectionPreferences.setTargetLongitude(directionModel.longitude.toFloat())
        DirectionPreferences.setTargetLabel(directionModel.name)
        DirectionPreferences.setUseMapsTarget(false)

        requireActivity().onBackPressed()
    }

    companion object {
        fun newInstance(): Directions {
            val args = Bundle()
            val fragment = Directions()
            fragment.arguments = args
            return fragment
        }
    }
}