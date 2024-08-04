package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.database.instances.MeasureDatabase
import app.simple.positional.extensions.viewmodel.WrappedViewModel
import app.simple.positional.model.Measure
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
            val measureDatabase =
                MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val entries = measureDatabase.measureDao()?.getAllMeasures()
            measureEntries.postValue(entries as ArrayList<Measure>)
        }
    }

    private fun loadMeasure(id: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val measureDatabase =
                MeasureDatabase.getInstance(getApplication<Application>().applicationContext)!!
            val measure = measureDatabase.measureDao()?.getMeasureById(id)
            this@MeasureViewModel.measure.postValue(measure)
        }
    }
}
