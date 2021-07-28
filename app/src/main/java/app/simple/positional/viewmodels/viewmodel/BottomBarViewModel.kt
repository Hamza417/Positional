package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarModel
import app.simple.positional.constants.LocationPins.locationsPins
import app.simple.positional.preferences.GPSPreferences.getPinSkin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomBarViewModel(application: Application) : AndroidViewModel(application) {

    val bottomBarData: MutableLiveData<ArrayList<BottomBarModel>> by lazy {
        MutableLiveData<ArrayList<BottomBarModel>>().also {
            loadBottomBarData()
        }
    }

    private fun loadBottomBarData() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext

            val list = arrayListOf(
                    BottomBarModel(R.drawable.ic_clock, "clock", context.getString(R.string.clock)),
                    BottomBarModel(R.drawable.ic_compass, "compass", context.getString(R.string.compass)),
                    BottomBarModel(locationsPins[getPinSkin()], "location", context.getString(R.string.gps_location)),
                    BottomBarModel(R.drawable.ic_trail_line, "trail", context.getString(R.string.trail)),
                    BottomBarModel(R.drawable.ic_level, "level", context.getString(R.string.level)),
                    // BottomBarModel(R.drawable.ic_settings, "settings", context.getString(R.string.settings))
            )

            bottomBarData.postValue(list)
        }
    }
}