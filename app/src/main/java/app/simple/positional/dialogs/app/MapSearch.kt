package app.simple.positional.dialogs.app

import android.graphics.Outline
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.decorations.views.SearchMap
import app.simple.positional.math.MathExtensions
import app.simple.positional.preferences.MainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MapSearch : CustomDialogFragment() {

    private lateinit var maps: SearchMap
    private lateinit var address: AutoCompleteTextView
    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    private var job: Job? = null

    private var callbacks: (address: String, latitude: Double, longitude: Double) -> Unit = { _, _, _ -> }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_map_search, container, false)

        maps = view.findViewById(R.id.map)
        address = view.findViewById(R.id.address)
        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        maps.onCreate(savedInstanceState)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitude.setText(MathExtensions.round(maps.lastLatitude, 6).toString())
        longitude.setText(MathExtensions.round(maps.lastLongitude, 6).toString())
        address.threshold = 5

        maps.callbacks = {
            latitude.setText(MathExtensions.round(it.latitude, 6).toString())
            longitude.setText(MathExtensions.round(it.longitude, 6).toString())
        }

        address.doOnTextChanged { text, _, _, _ ->
            try {
                job?.cancel(CancellationException("New search"))
            } catch (_: IllegalStateException) {
            }

            postDelayed(1000) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(requireContext()).getFromLocationName(text.toString(), 10) { addresses ->
                        postDelayed(0) {
                            Log.d("MapSearch", "Addresses: $addresses")
                            try {
                                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, addresses.map { it.getAddressLine(0) }).also {
                                    address.setAdapter(it)
                                    (address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                    (address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                    address.showDropDown()

                                    address.setOnItemClickListener { _, _, position, _ ->
                                        latitude.setText(MathExtensions.round(addresses[position].latitude, 6).toString())
                                        longitude.setText(MathExtensions.round(addresses[position].longitude, 6).toString())
                                        maps.moveCamera(addresses[position].latitude, addresses[position].longitude)
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
                        Geocoder(requireContext()).getFromLocationName(text.toString(), 10)?.let { addresses ->
                            launch(Dispatchers.Main) {
                                try {
                                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, addresses.map { it.getAddressLine(0) }).also {
                                        address.setAdapter(it)
                                        (address.adapter as ArrayAdapter<*>).setNotifyOnChange(true)
                                        (address.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                        address.showDropDown()

                                        address.setOnItemClickListener { _, _, position, _ ->
                                            latitude.setText(MathExtensions.round(addresses[position].latitude, 6).toString())
                                            longitude.setText(MathExtensions.round(addresses[position].longitude, 6).toString())
                                            maps.moveCamera(addresses[position].latitude, addresses[position].longitude)
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

        save.setOnClickListener {
            callbacks.invoke(address.text.toString(), latitude.text.toString().toDouble(), longitude.text.toString().toDouble())
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }

        /**
         * Add a custom clip bounds to the map
         */
        maps.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadiusDP = MainPreferences.getCornerRadius().toFloat()
                // val cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadiusDP, resources.displayMetrics)
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusDP)
            }
        }

        maps.clipToOutline = true
    }

    override fun getWindowWidth(): Int {
        val window = dialog!!.window ?: return 0
        val displayMetrics = DisplayMetrics()

        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)

        return (displayMetrics.widthPixels * 1f / 100f * 80F).toInt()
    }

    fun setOnMapSearch(callbacks: (address: String, latitude: Double, longitude: Double) -> Unit) {
        this.callbacks = callbacks
    }

    override fun onResume() {
        super.onResume()
        maps.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        maps.onDestroy()
        if (job?.isActive == true) job?.cancel(kotlinx.coroutines.CancellationException("Dialog destroyed"))
    }

    companion object {
        fun newInstance(): MapSearch {
            val args = Bundle()
            val fragment = MapSearch()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showMapSearch(): MapSearch {
            val fragment = MapSearch.newInstance()
            fragment.show(this, "map_search")
            return fragment
        }
    }
}