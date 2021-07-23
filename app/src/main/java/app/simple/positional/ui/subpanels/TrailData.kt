package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.trail.AdapterTrailData
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.viewmodels.factory.TrailDataFactory
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel

class TrailData : ScopedFragment() {

    private lateinit var trailDataViewModel: TrailDataViewModel
    private lateinit var recyclerView: CustomRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail_data, container, false)

        recyclerView = view.findViewById(R.id.trail_data_recycler_view)

        val trailDataFactory = TrailDataFactory(requireArguments().getString("trail_name")!!, requireActivity().application)
        trailDataViewModel = ViewModelProvider(requireActivity(), trailDataFactory).get(TrailDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trailDataViewModel.trailData.observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterTrailData(it)
        })
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