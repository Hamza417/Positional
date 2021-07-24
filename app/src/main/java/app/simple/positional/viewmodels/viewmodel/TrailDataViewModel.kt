package app.simple.positional.viewmodels.viewmodel

import android.app.Application
import android.text.Spanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.database.instances.TrailDataDatabase
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.model.TrailData
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.LocationExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class TrailDataViewModel(application: Application, private val trailName: String) : AndroidViewModel(application) {

    val trailDataAscending: MutableLiveData<ArrayList<TrailData>> by lazy {
        MutableLiveData<ArrayList<TrailData>>().also {
            loadTrailData(trailName)
        }
    }

    val trailDataDescending: MutableLiveData<ArrayList<TrailData>> by lazy {
        MutableLiveData<ArrayList<TrailData>>().also {
            loadTrailData(trailName)
        }
    }

    val trailDataDescendingWithInfo: MutableLiveData<Pair<ArrayList<TrailData>, Triple<String?, Spanned?, Spanned?>>> by lazy {
        MutableLiveData<Pair<ArrayList<TrailData>, Triple<String?, Spanned?, Spanned?>>>().also {
            loadTrailDataWithInformation(trailName)
        }
    }

    val trailInfo: MutableLiveData<Triple<String?, String?, String?>> by lazy {
        MutableLiveData<Triple<String?, String?, String?>>()
    }

    fun loadTrailData(trailName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                "$trailName.db").build()

            val list = database.trailDataDao()?.getAllTrailData()!!
            val listDesc = database.trailDataDao()?.getAllTrailDataDesc()!!
            database.close()

            trailDataAscending.postValue(list as ArrayList<TrailData>)

            delay(500) // For animation

            trailDataDescending.postValue(listDesc as ArrayList<TrailData>)
        }
    }

    fun saveTrailData(trailName: String, trails: TrailData) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                "$trailName.db").build()

            database.trailDataDao()?.insertTrailData(trails)!!

            database.close()
        }
    }

    fun deleteTrailData(trails: TrailData?) {
        viewModelScope.launch(Dispatchers.IO) {
            println(measureTimeMillis {
                trails ?: return@launch

                val database = Room.databaseBuilder(getApplication<Application>(),
                                                    TrailDataDatabase::class.java,
                                                    "$trailName.db").build()

                database.trailDataDao()?.deleteTrailData(trails)!!
                database.close()
            })
        }
    }

    private fun loadTrailDataWithInformation(trailName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(getApplication<Application>(),
                                                TrailDataDatabase::class.java,
                                                "$trailName.db").build()

            val list = database.trailDataDao()?.getAllTrailDataDesc()!!

            database.close()

            val total = HtmlHelper.fromHtml("<b>${getApplication<Application>().getString(R.string.total)} </b> ${list.size}")

            val builder = StringBuilder().also {
                it.append("<b>${getApplication<Application>().getString(R.string.gps_displacement)} </b>")

                val p0 = LocationExtension.measureDisplacement(list)

                if (MainPreferences.getUnit()) {
                    if (p0 < 1000) {
                        it.append(p0)
                        it.append(" ")
                        it.append(getApplication<Application>().getString(R.string.meter))
                    } else {
                        it.append(p0.toKilometers())
                        it.append(" ")
                        it.append(getApplication<Application>().getString(R.string.kilometer))
                    }
                } else {
                    if (p0 < 1000) {
                        it.append(p0.toDouble().toFeet())
                        it.append(" ")
                        it.append(getApplication<Application>().getString(R.string.feet))
                    } else {
                        it.append(p0.toMiles())
                        it.append(" ")
                        it.append(getApplication<Application>().getString(R.string.miles))
                    }
                }
            }

            val pair = Pair(list as ArrayList<TrailData>,
                            Triple(
                                    trailName,
                                    total,
                                    HtmlHelper.fromHtml(builder.toString())
                            ))

            delay(500)

            trailDataDescendingWithInfo.postValue(pair)
        }
    }
}
