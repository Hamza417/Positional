package app.simple.positional.ui.subpanels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.settings.AccentColorAdapter
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.preferences.MainPreferences

class AccentColor : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var accentColorAdapter: AccentColorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_color_accent, container, false)

        recyclerView = view.findViewById(R.id.accent_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        accentColorAdapter = AccentColorAdapter()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accentColorAdapter.setOnPaletteChangeListener(object : AccentColorAdapter.Companion.PalettesAdapterCallbacks {
            override fun onColorPressed(source: Int) {
                MainPreferences.setAccentColor(source)
            }
        })

        recyclerView.adapter = accentColorAdapter
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == MainPreferences.accentColor) {
            requireActivity().recreate()
        }
    }

    companion object {
        fun newInstance(): AccentColor {
            val args = Bundle()
            val fragment = AccentColor()
            fragment.arguments = args
            return fragment
        }
    }
}