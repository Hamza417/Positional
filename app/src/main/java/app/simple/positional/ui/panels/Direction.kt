package app.simple.positional.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.extensions.fragment.ScopedFragment

class Direction : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_direction, container, false)



        return view
    }

    companion object {
        fun newInstance(): Direction {
            val args = Bundle()
            val fragment = Direction()
            fragment.arguments = args
            return fragment
        }
    }
}