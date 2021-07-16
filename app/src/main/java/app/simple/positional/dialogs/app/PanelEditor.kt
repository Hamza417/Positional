package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.AdapterPanelEditor
import app.simple.positional.decorations.corners.DynamicCornerRecyclerView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.viewmodels.viewmodel.BottomBarViewModel

class PanelEditor : CustomBottomSheetDialogFragment() {

    private lateinit var recyclerView: DynamicCornerRecyclerView
    private val viewModel: BottomBarViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_panel_editor, container, false)

        recyclerView = view.findViewById(R.id.panel_editor_recycler_view)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bottomBarData.observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterPanelEditor(it)
        })
    }

    companion object {
        fun newInstance(): PanelEditor {
            val args = Bundle()
            val fragment = PanelEditor()
            fragment.arguments = args
            return fragment
        }
    }
}