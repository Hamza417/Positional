package app.simple.positional.activities.fragment

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import app.simple.positional.decorations.transitions.DetailsTransition
import app.simple.positional.decorations.transitions.DetailsTransitionArc
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

open class ScopedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    /**
     * clears the [setExitTransition] for the current fragment in support
     * for making the custom animations work for the fragments that needs
     * to originate from the current fragment
     */
    open fun clearExitTransition() {
        exitTransition = null
    }

    open fun clearEnterTransition() {
        enterTransition = null
    }

    /**
     * Sets fragment transitions prior to creating a new fragment.
     * Used with shared elements
     */
    open fun setTransitions() {
        /**
         * Animations are expensive, every time a view is added into the
         * animating view transaction time will increase a little
         * making the interaction a little bit slow.
         */
        exitTransition = Fade()
        enterTransition = Fade()
        sharedElementEnterTransition = DetailsTransitionArc()
        sharedElementReturnTransition = DetailsTransitionArc()
    }

    open fun setLinearTransitions() {
        /**
         * Animations are expensive, every time a view is added into the
         * animating view transaction time will increase a little
         * making the interaction a little bit slow.
         */
        exitTransition = Fade()
        enterTransition = Fade()
        sharedElementEnterTransition = DetailsTransition()
        sharedElementReturnTransition = DetailsTransition()
    }
}
