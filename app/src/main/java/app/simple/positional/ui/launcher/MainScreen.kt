package app.simple.positional.ui.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.mainscreen.MainScreenAdapter
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.ui.panels.*
import app.simple.positional.util.FragmentHelper
import app.simple.positional.viewmodels.viewmodel.MainScreenViewModel

class MainScreen : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var adapter: MainScreenAdapter

    private lateinit var settings: DynamicRippleImageButton

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)

        recyclerView = view.findViewById(R.id.main_list_rv)
        settings = view.findViewById(R.id.settings_button)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMainList().observe(viewLifecycleOwner, {
            adapter = MainScreenAdapter(it)

            adapter.onMainItemClicked = { v: View, i: Int ->
                when (i) {
                    0 -> {
                        FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Clock.newInstance(),
                                v,
                                "main_screen"
                        )
                    }
                    1 -> {
                        FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Compass.newInstance(),
                                v,
                                "main_screen"
                        )
                    }
                    2 -> {
                        FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                GPS.newInstance(),
                                v,
                                "main_screen"
                        )
                    }
                    3 -> {
                        FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Trail.newInstance(),
                                v,
                                "main_screen"
                        )
                    }
                    4 -> {
                        FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Level.newInstance(),
                                v,
                                "main_screen"
                        )
                    }
                }
            }

            recyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
            }
            recyclerView.adapter = adapter
        })

        settings.setOnClickListener {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.app_container, AppSettings.newInstance())
                    .addToBackStack("main_string")
                    .commit()
        }
    }

    companion object {
        fun newInstance(): MainScreen {
            val args = Bundle()
            val fragment = MainScreen()
            fragment.arguments = args
            return fragment
        }
    }
}