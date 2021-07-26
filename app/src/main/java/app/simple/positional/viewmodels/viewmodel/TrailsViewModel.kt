package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import app.simple.positional.database.instances.TrailDataDatabase
import app.simple.positional.database.instances.TrailDatabase
import app.simple.positional.model.TrailModel
import app.simple.positional.preferences.TrailPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrailsViewModel(application: Application) : AndroidViewModel(application) {

    val trails: MutableLiveData<ArrayList<TrailModel>> by lazy {
        MutableLiveData<ArrayList<TrailModel>>().also {
            loadTrails()
        }
    }

    fun getTrails(): LiveData<ArrayList<TrailModel>> {
        return trails
    }

    fun loadTrails() {
        viewModelScope.launch(Dispatchers.IO) {

            delay(500)

            val database = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                                TrailDatabase::class.java,
                                                "%%_trails.db").build()

            trails.postValue(database.trailDao()?.getAllTrails() as ArrayList<TrailModel>)

            database.close()
        }
    }

    fun addTrail(trailModel: TrailModel) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                              TrailDatabase::class.java,
                                              "%%_trails.db").build()

                db.trailDao()?.insertTrail(trailModel)

                TrailPreferences.setCurrentTrailName(trailModel.trailName)

                this@TrailsViewModel.trails.postValue(db.trailDao()!!.getAllTrails() as ArrayList<TrailModel>?)

                db.close()
            }
        }
    }

    fun deleteTrail(trailModel: TrailModel) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                              TrailDatabase::class.java,
                                              "%%_trails.db").build()

                db.trailDao()?.deleteTrail(trailModel)

                if (TrailPreferences.getCurrentTrail() == trailModel.trailName) {
                    TrailPreferences.setCurrentTrailName("")
                }

                this@TrailsViewModel.trails.postValue(db.trailDao()!!.getAllTrails() as ArrayList<TrailModel>?)

                db.close()

                clearResidualFiles(trailModel.trailName)
            }
        }
    }

    private fun clearResidualFiles(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                          TrailDataDatabase::class.java,
                                          "$name.db").build()

            db.trailDataDao()?.nukeTable()

            db.close()

            getApplication<Application>().applicationContext.deleteDatabase("$name.db")
        }
    }
}