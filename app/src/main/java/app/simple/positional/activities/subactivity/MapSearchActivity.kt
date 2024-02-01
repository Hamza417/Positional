package app.simple.positional.activities.subactivity

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.SearchMap
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.math.MathExtensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MapSearchActivity : BaseActivity() {

    private lateinit var map: SearchMap
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var address: AutoCompleteTextView
    private lateinit var confirm: DynamicRippleImageButton
    private lateinit var cancel: DynamicRippleImageButton

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_search)

        map = findViewById(R.id.map)
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
        address = findViewById(R.id.address)
        confirm = findViewById(R.id.confirm)
        cancel = findViewById(R.id.cancel)

        map.onCreate(savedInstanceState)

        latitude.text = MathExtensions.round(map.lastLatitude, 6).toString()
        longitude.text = MathExtensions.round(map.lastLongitude, 6).toString()
        address.threshold = 3

        map.callbacks = {
            latitude.text = MathExtensions.round(it.latitude, 6).toString()
            longitude.text = MathExtensions.round(it.longitude, 6).toString()
        }

        address.doOnTextChanged { text, _, _, _ ->
            try {
                job?.cancel(CancellationException("New search"))
            } catch (_: IllegalStateException) {
            }

            postDelayed(1000) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(applicationContext).getFromLocationName(text.toString(), 10) { addresses ->
                        runOnUiThread {
                            try {
                                ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, addresses.map { it.getAddressLine(0) }).also {
                                    address.setAdapter(it)
                                    (address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                    (address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                    address.showDropDown()

                                    address.setOnItemClickListener { _, _, position, _ ->
                                        latitude.text = MathExtensions.round(addresses[position].latitude, 6).toString()
                                        longitude.text = MathExtensions.round(addresses[position].longitude, 6).toString()
                                        map.moveCamera(addresses[position].latitude, addresses[position].longitude)
                                    }
                                }
                            } catch (e: IllegalStateException) {
                                Log.e("MapSearch", "IllegalStateException: ${e.message}")
                            }
                        }
                    }
                } else {
                    job = CoroutineScope(Dispatchers.IO).launch {
                        @Suppress("DEPRECATION")
                        Geocoder(applicationContext).getFromLocationName(text.toString(), 10)?.let { addresses ->
                            launch(Dispatchers.Main) {
                                try {
                                    ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, addresses.map { it.getAddressLine(0) }).also {
                                        address.setAdapter(it)
                                        (address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                        (address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                        address.showDropDown()

                                        address.setOnItemClickListener { _, _, position, _ ->
                                            latitude.text = MathExtensions.round(addresses[position].latitude, 6).toString()
                                            longitude.text = MathExtensions.round(addresses[position].longitude, 6).toString()
                                            map.moveCamera(addresses[position].latitude, addresses[position].longitude)
                                        }
                                    }
                                } catch (e: IllegalStateException) {
                                    Log.e("MapSearch", "IllegalStateException: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }

        confirm.setOnClickListener {
            setResult(RESULT_OK, intent.apply {
                putExtra("latitude", latitude.text.toString().toDouble())
                putExtra("longitude", longitude.text.toString().toDouble())
                putExtra("address", address.text.toString())
            })
            finish()
        }

        cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}