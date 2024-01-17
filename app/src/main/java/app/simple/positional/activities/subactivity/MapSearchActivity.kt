package app.simple.positional.activities.subactivity

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import app.simple.positional.databinding.ActivityMapSearchBinding
import app.simple.positional.extensions.activity.BaseActivity
import app.simple.positional.math.MathExtensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MapSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityMapSearchBinding
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.map.onCreate(savedInstanceState)

        binding.latitude.setText(MathExtensions.round(binding.map.lastLatitude, 6).toString())
        binding.longitude.setText(MathExtensions.round(binding.map.lastLongitude, 6).toString())
        binding.address.threshold = 3

        binding.map.callbacks = {
            binding.latitude.setText(MathExtensions.round(it.latitude, 6).toString())
            binding.longitude.setText(MathExtensions.round(it.longitude, 6).toString())
        }

        binding.address.doOnTextChanged { text, _, _, _ ->
            try {
                job?.cancel(CancellationException("New search"))
            } catch (_: IllegalStateException) {
            }

            postDelayed(1000) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(applicationContext).getFromLocationName(text.toString(), 10) { addresses ->
                        postDelayed(0) {
                            Log.d("MapSearch", "Addresses: $addresses")
                            try {
                                ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, addresses.map { it.getAddressLine(0) }).also {
                                    binding.address.setAdapter(it)
                                    (binding.address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                    (binding.address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                    binding.address.showDropDown()

                                    binding.address.setOnItemClickListener { _, _, position, _ ->
                                        binding.latitude.setText(MathExtensions.round(addresses[position].latitude, 6).toString())
                                        binding.longitude.setText(MathExtensions.round(addresses[position].longitude, 6).toString())
                                        binding.map.moveCamera(addresses[position].latitude, addresses[position].longitude)
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
                                        binding.address.setAdapter(it)
                                        (binding.address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                        (binding.address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                        binding.address.showDropDown()

                                        binding.address.setOnItemClickListener { _, _, position, _ ->
                                            binding.latitude.setText(MathExtensions.round(addresses[position].latitude, 6).toString())
                                            binding.longitude.setText(MathExtensions.round(addresses[position].longitude, 6).toString())
                                            binding.map.moveCamera(addresses[position].latitude, addresses[position].longitude)
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

        binding.confirm.setOnClickListener {
            setResult(RESULT_OK, intent.apply {
                putExtra("latitude", binding.latitude.text.toString().toDouble())
                putExtra("longitude", binding.longitude.text.toString().toDouble())
                putExtra("address", binding.address.text.toString())
            })
            finish()
        }

        binding.cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}