package app.simple.positional.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel

class TrailDataFactory(private val trailDataName: String, private val application: Application) :
        ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(TrailDataViewModel::class.java) -> {
                return TrailDataViewModel(application, trailDataName) as T
            }
            else -> {
                /**
                 * This viewmodel factory is specific to
                 * [TrailDataViewModel] and assigning it properly
                 * won't throw this exception
                 */
                throw IllegalArgumentException("Nope!!, Wrong Viewmodel!!")
            }
        }
    }
}