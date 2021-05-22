package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.AdapterIcons
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment

class Icons : CustomBottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_icon_selection, container, false)

        recyclerView = view.findViewById(R.id.icon_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = AdapterIcons()
    }

    companion object {
        fun newInstance(): Icons {
            val args = Bundle()
            val fragment = Icons()
            fragment.arguments = args
            return fragment
        }
    }
}
