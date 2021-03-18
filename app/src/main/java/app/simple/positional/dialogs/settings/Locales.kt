package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.LocaleAdapter
import app.simple.positional.callbacks.LocaleCallback
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.MainPreferences

class Locales : CustomBottomSheetDialogFragment(), LocaleCallback {

    private lateinit var recyclerView: RecyclerView
    private lateinit var localeAdapter: LocaleAdapter

    fun newInstance(): Locales {
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_locales, container, false)
        recyclerView = view.findViewById(R.id.locale_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        localeAdapter = LocaleAdapter()
        localeAdapter.localeCallback = this

        recyclerView.adapter = localeAdapter
        return view
    }

    override fun onLocaleSet(languageCode: String) {
        MainPreferences.setAppLanguage(languageCode)
        requireActivity().recreate()
    }
}