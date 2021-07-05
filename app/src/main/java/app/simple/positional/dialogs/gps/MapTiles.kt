package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.positional.R
import app.simple.positional.adapters.maps.MapTilesAdapter
import app.simple.positional.decorations.corners.DynamicCornerRecyclerView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.OSMPreferences

class MapTiles : CustomBottomSheetDialogFragment() {

    private lateinit var recyclerView: DynamicCornerRecyclerView
    private lateinit var adapter: MapTilesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_map_tiles, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_map_tiles)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = MapTilesAdapter()
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setOnMapTilesClickListener(object : MapTilesAdapter.Companion.MapTilesCallbacks {
            override fun onMapTilesSelected(string: String) {
                OSMPreferences.setMapTileProvider(string)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isDestroyed) {
            OSMMenu.newInstance()
                    .show(parentFragmentManager, "gps_menu")
        }
    }

    companion object {
        fun newInstance(): MapTiles {
            val args = Bundle()
            val fragment = MapTiles()
            fragment.arguments = args
            return fragment
        }
    }
}
