package app.simple.positional.ui.subpanels

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.adapters.direction.AdapterDirections
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.dialogs.direction.DirectionTarget
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.model.DirectionModel
import app.simple.positional.popups.directions.PopupDirectionAddMenu
import app.simple.positional.popups.directions.PopupDirectionsMenu
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.preferences.DirectionPreferences
import app.simple.positional.preferences.GPSPreferences
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
            PopupDirectionAddMenu(it).setOnPopupCallbacksListener(object : PopupDirectionAddMenu.Companion.PopupDirectionsAddCallbacks {
                override fun onNew() {
                    val p0 = DirectionTarget.newInstance()

                    p0.setOnDirectionTargetListener(object : DirectionTarget.Companion.DirectionTargetCallbacks {
                        override fun onDirectionAdded(directionModel: DirectionModel) {
                            directionsViewModel.addDirection(directionModel)
                        }
                    })

                    p0.show(childFragmentManager, "direction_target")
                }

                override fun onSaveFromTarget() {
                    val directionModel = DirectionModel(
                            GPSPreferences.getTargetMarkerCoordinates()[0].toDouble(),
                            GPSPreferences.getTargetMarkerCoordinates()[1].toDouble(),
                            "",
                            System.currentTimeMillis())

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

        directionsViewModel.getDirections().observe(viewLifecycleOwner) {
            val adapter = AdapterDirections(it)

            adapter.setOnDirectionCallbacksListener(object : AdapterDirections.Companion.AdapterDirectionsCallbacks {
                override fun onDirectionClicked(directionModel: DirectionModel) {
                    set(directionModel)
                }

                override fun onDirectionLongPressed(directionModel: DirectionModel) {

                }

                override fun onMenuPressed(directionModel: DirectionModel, view: View) {
                    PopupDirectionsMenu(view).setOnPopupCallbacksListener(object : PopupDirectionsMenu.Companion.PopupDirectionsCallbacks {
                        override fun onSet() {
                            set(directionModel)
                        }

                        override fun onDelete() {
                            DeletePopupMenu(view) {
                                directionsViewModel.deleteDirection(directionModel)
                            }
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

                        override fun onUseAsTarget() {
                            GPSPreferences.setTargetMarker(true)
                            GPSPreferences.setTargetMarkerLatitude(directionModel.latitude.toFloat())
                            GPSPreferences.setTargetMarkerLongitude(directionModel.longitude.toFloat())
                            GPSPreferences.setTargetMarkerStartLatitude(MainPreferences.getLastCoordinates()[0])
                            GPSPreferences.setTargetMarkerStartLongitude(MainPreferences.getLastCoordinates()[1])
                            set(directionModel, useMapsTarget = true)
                        }

                        override fun onNavigate() {
                            kotlin.runCatching {
                                val uri: Uri = Uri.parse("google.navigation:q=" + directionModel.latitude.toString() + "," + directionModel.longitude.toString() + "&mode=d")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                startActivity(intent)
                            }.getOrElse {
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            })

            recyclerView.adapter = adapter
        }
    }

    private fun set(directionModel: DirectionModel, useMapsTarget: Boolean = false) {
        DirectionPreferences.setTargetLatitude(directionModel.latitude.toFloat())
        DirectionPreferences.setTargetLongitude(directionModel.longitude.toFloat())
        DirectionPreferences.setTargetLabel(directionModel.name)
        DirectionPreferences.setUseMapsTarget(useMapsTarget)

        requireActivity().onBackPressedDispatcher.onBackPressed()
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
