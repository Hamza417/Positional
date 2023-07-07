package app.simple.positional.ui.subpanels

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.positional.R
import app.simple.positional.adapters.settings.AccentColorAdapter
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.AppUtils

class AccentColor : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var accentColorAdapter: AccentColorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_color_accent, container, false)

        recyclerView = view.findViewById(R.id.accent_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        val list = arrayListOf(
                Pair(ContextCompat.getColor(requireContext(), R.color.positional), "Positional"),
                Pair(ContextCompat.getColor(requireContext(), R.color.blue), "Blue"),
                Pair(ContextCompat.getColor(requireContext(), R.color.blueGrey), "Blue Grey"),
                Pair(ContextCompat.getColor(requireContext(), R.color.darkBlue), "Dark Blue"),
                Pair(ContextCompat.getColor(requireContext(), R.color.red), "Red"),
                Pair(ContextCompat.getColor(requireContext(), R.color.green), "Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.orange), "Orange"),
                Pair(ContextCompat.getColor(requireContext(), R.color.purple), "Purple"),
                Pair(ContextCompat.getColor(requireContext(), R.color.yellow), "Yellow"),
                Pair(ContextCompat.getColor(requireContext(), R.color.caribbeanGreen), "Caribbean Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.persianGreen), "Persian Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.amaranth), "Amaranth"),
                Pair(ContextCompat.getColor(requireContext(), R.color.indian_red), "Indian Red"),
                Pair(ContextCompat.getColor(requireContext(), R.color.light_coral), "Light Coral"),
                Pair(ContextCompat.getColor(requireContext(), R.color.pink_flare), "Pink Flare"),
                Pair(ContextCompat.getColor(requireContext(), R.color.makeup_tan), "Makeup Tan"),
                Pair(ContextCompat.getColor(requireContext(), R.color.egg_yellow), "Egg Yellow"),
                Pair(ContextCompat.getColor(requireContext(), R.color.medium_green), "Medium Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.olive), "Olive"),
                Pair(ContextCompat.getColor(requireContext(), R.color.copperfield), "Copperfield"),
                Pair(ContextCompat.getColor(requireContext(), R.color.mineral_green), "Mineral Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.lochinvar), "Lochinvar"),
                Pair(ContextCompat.getColor(requireContext(), R.color.beach_grey), "Beach Grey"),
        )

        if (AppUtils.isFullFlavor()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                list.add(1, Pair(ContextCompat.getColor(requireContext(), android.R.color.system_accent1_500), "Material You (Dynamic)"))
            }
        }

        accentColorAdapter = AccentColorAdapter(list)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accentColorAdapter.setOnPaletteChangeListener(object : AccentColorAdapter.Companion.PalettesAdapterCallbacks {
            override fun onColorPressed(source: Int) {
                if (source == ContextCompat.getColor(requireContext(), android.R.color.system_accent1_500)) {
                    MainPreferences.setAccentColor(source)
                    MainPreferences.setMaterialYouAccentColor(true)
                } else {
                    MainPreferences.setAccentColor(source)
                    MainPreferences.setMaterialYouAccentColor(false)
                }
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