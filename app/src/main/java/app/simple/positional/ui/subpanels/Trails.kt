package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.trail.AdapterTrails
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.dialogs.trail.AddTrail
import app.simple.positional.model.TrailModel
import app.simple.positional.popups.DeletePopupMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ViewUtils
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel
import com.google.android.material.card.MaterialCardView

class Trails : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var adapterTrails: AdapterTrails
    private lateinit var add: MaterialCardView
    private val trailViewModel: TrailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trails_list, container, false)

        recyclerView = view.findViewById(R.id.trails_recycler_view)
        add = view.findViewById(R.id.add)

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

        trailViewModel.trails.observe(viewLifecycleOwner, {
            adapterTrails = AdapterTrails(it)
            adapterTrails.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            adapterTrails.setOnAdapterTrailsCallbackListener(object : AdapterTrails.Companion.AdapterTrailsCallback {
                override fun onTrailClicked(name: String) {
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.sub_container, TrailData.newInstance(name))
                        .addToBackStack(tag)
                        .commit()
                }

                override fun onDelete(trailModel: TrailModel, anchor: View) {
                    val popup = DeletePopupMenu(
                            layoutInflater.inflate(R.layout.popup_delete_confirmation,
                                                   DynamicCornerLinearLayout(requireContext())), anchor)

                    popup.setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                        override fun delete() {
                            trailViewModel.deleteTrail(trailModel)
                        }
                    })
                }
            })

            recyclerView.adapter = adapterTrails
        })
    }

    override fun onResume() {
        super.onResume()
        trailViewModel.loadTrails()
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