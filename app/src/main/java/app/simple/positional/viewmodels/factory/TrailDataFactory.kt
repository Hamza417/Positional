package app.simple.positional.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel

class TrailDataFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when (modelClass) {
            TrailDataViewModel::class.java -> {
                return TrailDataViewModel(checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown class $modelClass")
            }
        }
    }
}