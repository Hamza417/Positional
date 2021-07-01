package app.simple.positional.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.decorations.trail.TrailMaps
import app.simple.positional.util.NullSafety.isNotNull

class Trail : ScopedFragment() {

    private lateinit var maps: TrailMaps

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail, container, false)

        maps = view.findViewById(R.id.map_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        maps.onCreate(savedInstanceState)
        maps.resume()
    }

    override fun onResume() {
        super.onResume()
        if (maps.isNotNull()) {
            maps.resume()
        }
    }

    companion object {

        fun newInstance(): Trail {
            val args = Bundle()
            val fragment = Trail()
            fragment.arguments = args
            return fragment
        }
    }
}