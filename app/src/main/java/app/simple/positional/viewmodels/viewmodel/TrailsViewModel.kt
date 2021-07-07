package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import app.simple.positional.database.instances.TrailDatabase
import app.simple.positional.model.Trails
import app.simple.positional.preferences.TrailPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrailsViewModel(application: Application) : AndroidViewModel(application) {

    private val trails: MutableLiveData<ArrayList<Trails>> by lazy {
        MutableLiveData<ArrayList<Trails>>().also {
            loadTrails()
        }
    }

    fun getTrails(): LiveData<ArrayList<Trails>> {
        return trails
    }

    fun loadTrails() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                                TrailDatabase::class.java,
                                                "%%_trails.db").build()

            trails.postValue(database.trailDao()!!.getAllTrails() as ArrayList<Trails>)

            database.close()
        }
    }

    fun addTrail(trails: Trails) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                          TrailDatabase::class.java,
                                          "%%_trails.db").build()

            db.trailDao()?.insertTrail(trails)

            TrailPreferences.setLastTrailName(trails.trailName)

            this@TrailsViewModel.trails.postValue(db.trailDao()!!.getAllTrails() as ArrayList<Trails>?)

            db.close()
        }
    }
}