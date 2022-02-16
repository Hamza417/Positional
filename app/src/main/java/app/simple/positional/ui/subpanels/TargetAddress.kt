package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.adapters.location.AdapterTargetAddress
import app.simple.positional.decorations.searchview.SearchView
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.util.StatusBarHeight
import app.simple.positional.viewmodels.viewmodel.AddressViewModel

class TargetAddress : ScopedFragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomRecyclerView

    private val addressViewModel: AddressViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_target_address_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.addresses_rv)

        val params = searchView.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin,
                          StatusBarHeight.getStatusBarHeight(resources) + params.topMargin,
                          params.rightMargin,
                          params.bottomMargin)

        recyclerView.setPadding(recyclerView.paddingLeft,
                                recyclerView.paddingTop + params.topMargin + params.height + params.bottomMargin,
                                recyclerView.paddingRight,
                                recyclerView.paddingBottom + params.bottomMargin)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView.setSearchViewEventListener { keywords, _ ->
            addressViewModel.getCoordinatesFromAddress(keywords)
        }

        addressViewModel.address.observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterTargetAddress(it)
        }
    }

    companion object {
        fun newInstance(): TargetAddress {
            val args = Bundle()
            val fragment = TargetAddress()
            fragment.arguments = args
            return fragment
        }
    }
}