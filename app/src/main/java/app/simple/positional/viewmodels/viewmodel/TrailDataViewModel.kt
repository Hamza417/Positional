package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import app.simple.positional.database.instances.TrailDataDatabase
import app.simple.positional.model.TrailData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrailDataViewModel(application: Application, private val trailDataName: String) : AndroidViewModel(application) {

    val trailData: MutableLiveData<MutableList<TrailData>> by lazy {
        MutableLiveData<MutableList<TrailData>>().also {
            loadTrailData(trailDataName)
        }
    }

    fun getTrailData(): LiveData<MutableList<TrailData>> {
        return trailData
    }

    private fun loadTrailData(trailName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                trailName).build()

            val list = database.trailDataDao()?.getAllTrailData()
            database.close()

            trailData.postValue(list)
        }
    }
}