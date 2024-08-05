package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.database.instances.MeasureDatabase
import app.simple.positional.extensions.viewmodel.WrappedViewModel
import app.simple.positional.model.Measure
import app.simple.positional.model.MeasurePoint
import app.simple.positional.preferences.MeasurePreferences
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeasureViewModel(application: Application) : WrappedViewModel(application) {

    private val measureEntries: MutableLiveData<ArrayList<Measure>> by lazy {
        MutableLiveData<ArrayList<Measure>>().also {
            loadMeasureEntries()
        }
    }

    private val measure: MutableLiveData<Measure> by lazy {
        MutableLiveData<Measure>().also {
            loadMeasure(MeasurePreferences.getLastSelectedMeasure())
        }
    }

    fun getMeasureEntries(): LiveData<ArrayList<Measure>> {
        return measureEntries
    }

    fun getMeasure(): LiveData<Measure> {
        return measure
    }

    private fun loadMeasureEntries() {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val entries = measureDatabase.measureDao()?.getAllMeasures()
            measureEntries.postValue(entries as ArrayList<Measure>)
        }
    }

    private fun loadMeasure(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val measure = measureDatabase.measureDao()?.getMeasureById(id)
            this@MeasureViewModel.measure.postValue(measure)
        }
    }

    fun addMeasure(name: String, note: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val measure = Measure(name, note)
            measureDatabase.measureDao()?.insertMeasure(measure)
            MeasurePreferences.setLastSelectedMeasure(measure.dateCreated)
            loadMeasure(measure.dateCreated)
        }
    }

    fun addMeasurePoint(latLng: LatLng) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val measure = measure.value
            measure?.let {
                val measurePoint = MeasurePoint(
                    latLng.latitude, latLng.longitude, measure.measurePoints?.size?.plus(1) ?: 0)
                measure.measurePoints?.add(measurePoint)
                measureDatabase.measureDao()?.updateMeasure(measure)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MeasureDatabase.destroyInstance()
    }

    fun removeMeasurePoint(measurePoint: MeasurePoint?) {
        viewModelScope.launch(Dispatchers.Default) {
            measurePoint?.let {
                val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
                val measure = measure.value
                measure?.let {
                    measurePoint.let {
                        measure.measurePoints?.remove(it)
                        measureDatabase.measureDao()?.updateMeasure(measure)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MeasureViewModel"
    }
}
