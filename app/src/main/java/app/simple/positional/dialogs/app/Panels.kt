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
import app.simple.positional.model.BottomBar
import app.simple.positional.ui.panels.Measure
import app.simple.positional.util.AppUtils
import app.simple.positional.util.StatusBarHeight

class Panels : CustomBottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private var panelsCallback: PanelsCallback? = null
    private var bottomBarItems: ArrayList<BottomBar>? = null

    private var isLandscape = false
    private var spanCount = 4

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_panel_switcher, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        isLandscape = StatusBarHeight.isLandscape(requireContext())

        spanCount = when {
            isLandscape -> LANDSCAPE_SPAN_COUNT
            else -> PORTRAIT_SPAN_COUNT
        }

        bottomBarItems = BottomBarItems.getBottomBarItems(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (AppUtils.isLiteFlavor()) {
            bottomBarItems?.removeIf {
                it.tag == Measure.TAG
            }
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = AdapterPanels(bottomBarItems!!) { view1, name, position ->
            panelsCallback?.onPanelClick(view1, name, position)
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

        fun FragmentManager.showPanelsDialog(panelsCallback: PanelsCallback): Panels {
            val dialog = newInstance()
            dialog.apply {
                setPanelsCallback(panelsCallback)
            }
            dialog.show(this, TAG)
            return dialog
        }

        private const val TAG = "Panels"
        private const val PORTRAIT_SPAN_COUNT = 4
        private const val LANDSCAPE_SPAN_COUNT = 7
    }
}
