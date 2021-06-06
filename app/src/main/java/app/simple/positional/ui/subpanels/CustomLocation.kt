package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment

class CustomLocation : ScopedFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_location, container, false)

        return view
    }

    companion object {
        fun newInstance(): CustomLocation {
            val args = Bundle()
            val fragment = CustomLocation()
            fragment.arguments = args
            return fragment
        }
    }
}