package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.preferences.MainPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddressViewModel(application: Application) : AndroidViewModel(application) {

    val coordinates: MutableLiveData<Pair<Double, Double>> = MutableLiveData()
    val address: MutableLiveData<MutableList<Address>> = MutableLiveData()

    fun getCoordinatesFromAddress(address: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                val geocoder = Geocoder(getApplication())
                var addresses: MutableList<Address>?

                withContext(Dispatchers.IO) {
                    runCatching {
                        MainPreferences.setAddress(address)

                        @Suppress("DEPRECATION")
                        addresses = geocoder.getFromLocationName(address, 5)

                        coordinates.postValue(Pair(addresses!![0].latitude, addresses!![0].longitude))
                        this@AddressViewModel.address.postValue(addresses)
                    }
                }
            }
        }
    }
}