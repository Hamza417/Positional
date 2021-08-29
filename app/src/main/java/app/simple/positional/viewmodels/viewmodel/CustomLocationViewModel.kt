package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import app.simple.positional.database.instances.LocationDatabase
import app.simple.positional.model.Locations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomLocationViewModel(application: Application) : AndroidViewModel(application) {

    val customLocations: MutableLiveData<MutableList<Locations>> by lazy {
        MutableLiveData<MutableList<Locations>>().also {
            loadLocations()
        }
    }

    val artState = MutableLiveData<Boolean>()

    private fun loadLocations() {
        viewModelScope.launch(Dispatchers.Default) {
            val db = Room.databaseBuilder(
                    getApplication<Application>(),
                    LocationDatabase::class.java,
                    "locations.db")
                    .fallbackToDestructiveMigration()
                    .build()

            with(db.locationDao()!!.getAllLocations()) {
                artState.postValue(this.isEmpty())
                customLocations.postValue(this)
            }

            db.close()
        }
    }

    fun saveLocation(locations: Locations) {
        viewModelScope.launch(Dispatchers.Default) {
            val db = Room.databaseBuilder(
                    getApplication<Application>(),
                    LocationDatabase::class.java,
                    "locations.db")
                    .fallbackToDestructiveMigration()
                    .build()

            db.locationDao()?.insertLocation(locations)

            with(db.locationDao()!!.getAllLocations()) {
                artState.postValue(this.isEmpty())
            }

            db.close()
        }
    }

    fun deleteAll() {
       viewModelScope.launch(Dispatchers.Default) {
           val db = Room.databaseBuilder(getApplication<Application>(), LocationDatabase::class.java, "locations.db").build()
           db.locationDao()?.nukeTable()

           with(db.locationDao()!!.getAllLocations()) {
               artState.postValue(this.isEmpty())
               customLocations.postValue(this)
           }

           db.close()
       }
    }
}