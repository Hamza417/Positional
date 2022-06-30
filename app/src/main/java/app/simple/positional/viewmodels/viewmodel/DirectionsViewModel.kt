package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.database.instances.DirectionDatabase
import app.simple.positional.extensions.viewmodel.WrappedViewModel
import app.simple.positional.model.DirectionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DirectionsViewModel(application: Application) : WrappedViewModel(application) {

    private val directions: MutableLiveData<MutableList<DirectionModel>> by lazy {
        MutableLiveData<MutableList<DirectionModel>>().also {
            loadDirections()
        }
    }

    val added: MutableLiveData<Int> = MutableLiveData()

    fun getDirections(): LiveData<MutableList<DirectionModel>> {
        return directions
    }

    private fun loadDirections() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DirectionDatabase.getInstance(getApplication())
            directions.postValue(db?.directionDao()?.getAllDirections())
            db?.close()
        }
    }

    fun addDirection(directionModel: DirectionModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DirectionDatabase.getInstance(getApplication())
            db?.directionDao()?.insertDirection(directionModel)
            directions.postValue(db?.directionDao()?.getAllDirections())
            added.postValue((added.value ?: -1).plus(1))
            db?.close()
        }
    }

    fun deleteDirection(directionModel: DirectionModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DirectionDatabase.getInstance(getApplication())
            db?.directionDao()?.deleteDirection(directionModel)
            directions.postValue(db?.directionDao()?.getAllDirections())
            db?.close()
        }
    }
}