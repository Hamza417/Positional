package app.simple.positional.ui.panels

import android.os.Bundle
import app.simple.positional.extensions.fragment.ScopedFragment

class Measure : ScopedFragment() {

    companion object {
        fun newInstance(): Measure {
            val args = Bundle()
            val fragment = Measure()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Measure"
    }
}
