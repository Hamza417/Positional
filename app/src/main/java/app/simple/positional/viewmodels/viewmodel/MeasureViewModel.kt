package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.database.instances.MeasureDatabase
import app.simple.positional.extensions.viewmodel.WrappedViewModel
import app.simple.positional.model.Measure
import app.simple.positional.model.MeasurePoint
import app.simple.positional.preferences.MeasurePreferences
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

    fun addMeasurePoint(measurePoint: MeasurePoint, onAdded: (Measure) -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val measure = measure.value
            measure?.let {
                measure.measurePoints?.add(measurePoint)
                measureDatabase.measureDao()?.updateMeasure(measure)
                onAdded(measure)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MeasureDatabase.destroyInstance()
    }

    fun removeMeasurePoint(measurePoint: MeasurePoint?, onRemoved: (Measure) -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            measurePoint?.let {
                val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
                val measure = measure.value
                measure?.let {
                    measurePoint.let {
                        measure.measurePoints?.remove(it)
                        measureDatabase.measureDao()?.updateMeasure(measure)
                        onRemoved(measure)
                    }
                }
            }
        }
    }

    fun deleteMeasure(measure: Measure, onDeleted: (Measure) -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase = MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            measureDatabase.measureDao()?.deleteMeasure(measure)
            MeasurePreferences.setLastSelectedMeasure(-1)
            onDeleted(measure)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MeasurePreferences.LAST_SELECTED_MEASURE -> {
                loadMeasure(MeasurePreferences.getLastSelectedMeasure())
            }
        }
    }

    companion object {
        private const val TAG = "MeasureViewModel"
    }
}
