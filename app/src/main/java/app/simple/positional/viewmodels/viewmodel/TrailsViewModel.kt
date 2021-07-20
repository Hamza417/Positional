package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import app.simple.positional.database.instances.TrailDatabase
import app.simple.positional.model.TrailModel
import app.simple.positional.preferences.TrailPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrailsViewModel(application: Application) : AndroidViewModel(application) {

    val trailModel: MutableLiveData<ArrayList<TrailModel>> by lazy {
        MutableLiveData<ArrayList<TrailModel>>().also {
            loadTrails()
        }
    }

    fun loadTrails() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                                TrailDatabase::class.java,
                                                "%%_trails.db").build()

            trailModel.postValue(database.trailDao()!!.getAllTrails() as ArrayList<TrailModel>)

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

                TrailPreferences.setLastTrailName(trailModel.trailName)

                this@TrailsViewModel.trailModel.postValue(db.trailDao()!!.getAllTrails() as ArrayList<TrailModel>?)

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

                if (TrailPreferences.getLastUsedTrail() == trailModel.trailName) {
                    TrailPreferences.setLastTrailName("")
                }

                this@TrailsViewModel.trailModel.postValue(db.trailDao()!!.getAllTrails() as ArrayList<TrailModel>?)

                db.close()
            }
        }
    }
}