package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.app.AdapterPanels
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.callbacks.PanelsCallback
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment

class Panels : CustomBottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private var panelsCallback: PanelsCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_panel_switcher, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.adapter = AdapterPanels(BottomBarItems.getBottomBarItems(requireContext())) { view, name, position ->
            panelsCallback?.onPanelClick(view, name, position)
            dismiss()
        }
    }

    fun setPanelsCallback(panelsCallback: PanelsCallback) {
        this.panelsCallback = panelsCallback
    }

    companion object {
        fun newInstance(): Panels {
            val args = Bundle()
            val fragment = Panels()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showPanelsDialog(panelsCallback: PanelsCallback) {
            newInstance().apply {
                setPanelsCallback(panelsCallback)
            }.show(this, Panels::class.java.simpleName)
        }
    }
}