package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.trail.AdapterTrailData
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ViewUtils
import app.simple.positional.util.ViewUtils.makeInvisible
import app.simple.positional.util.ViewUtils.makeVisible
import app.simple.positional.viewmodels.factory.TrailDataFactory
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel
import com.google.android.material.card.MaterialCardView

class TrailData : ScopedFragment() {

    private lateinit var trailDataViewModel: TrailDataViewModel
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var setTrail: MaterialCardView
    private lateinit var art: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail_data, container, false)

        recyclerView = view.findViewById(R.id.trail_data_recycler_view)
        setTrail = view.findViewById(R.id.set_trail)
        art = view.findViewById(R.id.art)

        val trailDataFactory = TrailDataFactory(requireArguments().getString("trail_name")!!, requireActivity().application)
        trailDataViewModel = ViewModelProvider(this, trailDataFactory).get(TrailDataViewModel::class.java)

        setTrail.apply {
            radius = MainPreferences.getCornerRadius().toFloat()
            cardElevation = 50F
            ViewUtils.addShadow(this)
        }

        recyclerView.apply {
            setPadding(
                    paddingLeft,
                    paddingTop,
                    paddingRight,
                    paddingBottom + setTrail.marginBottom + setTrail.marginTop + resources.getDimensionPixelSize(R.dimen.float_button_size)
            )
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trailDataViewModel.trailDataDescendingWithInfo.observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterTrailData(it)

            if (it.first.isNullOrEmpty()) {
                art.makeVisible(true)
            } else {
                art.makeInvisible(true)
            }
        })

        setTrail.setOnClickListener {
            TrailPreferences.setLastTrailName(requireArguments().getString("trail_name")!!)
            requireActivity().finish()
        }
    }

    companion object {
        fun newInstance(trailName: String): TrailData {
            val args = Bundle()
            args.putString("trail_name", trailName)
            val fragment = TrailData()
            fragment.arguments = args
            return fragment
        }
    }
}