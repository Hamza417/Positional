package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import app.simple.positional.database.instances.TrailDatabase
import app.simple.positional.database.instances.TrailPointDatabase
import app.simple.positional.model.TrailEntry
import app.simple.positional.preferences.TrailPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrailsViewModel(application: Application) : AndroidViewModel(application) {

    val trails: MutableLiveData<ArrayList<TrailEntry>> by lazy {
        MutableLiveData<ArrayList<TrailEntry>>().also {
            loadTrails()
        }
    }

    fun getTrails(): LiveData<ArrayList<TrailEntry>> {
        return trails
    }

    fun loadTrails() {
        viewModelScope.launch(Dispatchers.IO) {

            delay(500)

            val database = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                                TrailDatabase::class.java,
                                                "%%_trails.db").build()

            trails.postValue(database.trailDao()?.getAllTrails() as ArrayList<TrailEntry>)

            database.close()
        }
    }

    fun addTrail(trailEntry: TrailEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                              TrailDatabase::class.java,
                                              "%%_trails.db").build()

                db.trailDao()?.insertTrail(trailEntry)

                TrailPreferences.setCurrentTrailName(trailEntry.trailName)

                this@TrailsViewModel.trails.postValue(db.trailDao()!!
                    .getAllTrails() as ArrayList<TrailEntry>?)

                db.close()
            }
        }
    }

    fun deleteTrail(trailEntry: TrailEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                              TrailDatabase::class.java,
                                              "%%_trails.db").build()

                db.trailDao()?.deleteTrail(trailEntry)

                if (TrailPreferences.getCurrentTrail() == trailEntry.trailName) {
                    TrailPreferences.setCurrentTrailName("")
                }

                this@TrailsViewModel.trails.postValue(db.trailDao()!!
                    .getAllTrails() as ArrayList<TrailEntry>?)

                db.close()

                clearResidualFiles(trailEntry.trailName)
            }
        }
    }

    private fun clearResidualFiles(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                TrailPointDatabase::class.java,
                                          "$name.db").build()

            db.trailDataDao()?.nukeTable()

            db.close()

            getApplication<Application>().applicationContext.deleteDatabase("$name.db")
        }
    }
}
