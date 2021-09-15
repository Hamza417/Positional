package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.R
import app.simple.positional.constants.LocationPins
import app.simple.positional.model.MainListModel
import app.simple.positional.preferences.GPSPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val mainList: MutableLiveData<ArrayList<MainListModel>> by lazy {
        MutableLiveData<ArrayList<MainListModel>>().also {
            loadMainListData()
        }
    }

    fun getMainList(): LiveData<ArrayList<MainListModel>> {
        return mainList
    }

    private fun loadMainListData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list =
                    with(getApplication<Application>()) {
                        arrayListOf(
                                MainListModel(getString(R.string.clock), R.drawable.sc_01_clock),
                                MainListModel(getString(R.string.compass), R.drawable.sc_02_compass),
                                MainListModel(getString(R.string.gps_location), R.drawable.sc_03_gps),
                                MainListModel(getString(R.string.trail), R.drawable.ic_trail_line),
                                MainListModel(getString(R.string.level), R.drawable.sc_04_level)
                        )
                    }

            delay(500L)

            mainList.postValue(list)
        }
    }
}