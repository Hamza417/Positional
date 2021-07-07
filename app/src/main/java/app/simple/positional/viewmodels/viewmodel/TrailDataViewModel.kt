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
import kotlin.system.measureTimeMillis

class TrailDataViewModel(application: Application, private val trailName: String) : AndroidViewModel(application) {

    private val trailData: MutableLiveData<ArrayList<TrailData>> by lazy {
        MutableLiveData<ArrayList<TrailData>>().also {
            loadTrailData(trailName)
        }
    }

    fun getTrailData(): LiveData<ArrayList<TrailData>> {
        return trailData
    }

    private fun loadTrailData(trailName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                "$trailName.db").build()

            val list = database.trailDataDao()?.getAllTrailData()
            database.close()

            trailData.postValue(list as ArrayList<TrailData>)
        }
    }

    fun saveTrailData(trailName: String, trails: TrailData) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                "$trailName.db").build()

            database.trailDataDao()?.insertTrailData(trails)!!

            database.close()
        }
    }

    fun deleteTrailData(trails: TrailData?) {
        viewModelScope.launch(Dispatchers.IO) {
            println(measureTimeMillis {
                trails ?: return@launch

                val database = Room.databaseBuilder(getApplication<Application>(),
                                                    TrailDataDatabase::class.java,
                                                    "$trailName.db").build()

                database.trailDataDao()?.deleteTrailData(trails)!!
                database.close()
            })
        }
    }
}
