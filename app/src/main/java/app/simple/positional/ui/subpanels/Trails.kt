package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.trail.AdapterTrails
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.dialogs.trail.AddTrail
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.model.TrailEntry
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.popups.trail.PopupTrailsMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ViewUtils
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel
import com.google.android.material.card.MaterialCardView

class Trails : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var adapterTrails: AdapterTrails
    private lateinit var add: MaterialCardView
    private lateinit var art: ImageView
    private val trailViewModel: TrailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trails_list, container, false)

        recyclerView = view.findViewById(R.id.trails_recycler_view)
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

        add.setOnClickListener {
            val p0 = AddTrail.newInstance()

            p0.onNewTrailAddedSuccessfully = {
                trailViewModel.addTrail(it)
            }

            p0.show(parentFragmentManager, "add_trail")
        }

        trailViewModel.getTrails().observe(viewLifecycleOwner) {
            adapterTrails = AdapterTrails(it)
            adapterTrails.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            adapterTrails.setOnAdapterTrailsCallbackListener(object : AdapterTrails.Companion.AdapterTrailsCallback {
                override fun onTrailClicked(name: String) {
                    TrailPreferences.setCurrentTrailName(name)
                    requireActivity().finish()
                }

                override fun onTrailMenu(trailEntry: TrailEntry, anchor: View) {
                    val popup = PopupTrailsMenu(
                            layoutInflater.inflate(R.layout.popup_trails,
                                    DynamicCornerLinearLayout(requireContext())), anchor)


                    popup.setOnPopupCallbacksListener(object : PopupTrailsMenu.Companion.PopupTrailsCallbacks {
                        override fun onShowOnMap() {
                            TrailPreferences.setCurrentTrailName(trailEntry.trailName).also {
                                requireActivity().finish()
                            }
                        }

                        override fun onDelete() {
                            val deletePopupMenu = DeletePopupMenu(
                                    anchor)

                            deletePopupMenu.setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                                override fun delete() {
                                    trailViewModel.deleteTrail(trailEntry)
                                }
                            })
                        }
                    })
                }
            })

            if (it.isNullOrEmpty()) {
                art.visible(true)
            } else {
                art.invisible(true)
            }

            recyclerView.adapter = adapterTrails
        }
    }

    companion object {
        fun newInstance(): Trails {
            val args = Bundle()
            val fragment = Trails()
            fragment.arguments = args
            return fragment
        }
    }
}
