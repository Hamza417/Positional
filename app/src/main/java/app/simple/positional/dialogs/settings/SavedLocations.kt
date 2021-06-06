package app.simple.positional.dialogs.settings

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.adapters.LocationsAdapter
import app.simple.positional.callbacks.LocationAdapterCallback
import app.simple.positional.database.LocationDatabase
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.model.Locations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedLocations : CustomDialogFragment(), LocationAdapterCallback {

    fun newInstance(): SavedLocations {
        val args = Bundle()
        this.arguments = args
        return this
    }

    var locationAdapterCallback: LocationAdapterCallback? = null
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var art: ImageView
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_all_locations, container, false)

        recyclerView = view.findViewById(R.id.locations_recycler_view)
        clear = view.findViewById(R.id.delete_locations)
        art = view.findViewById(R.id.no_locations_found)
        locationsAdapter = LocationsAdapter()
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)

        locationsAdapter.locationsAdapterCallback = this
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val db = Room.databaseBuilder(
                    requireContext(),
                    LocationDatabase::class.java,
                    "locations.db")
                    .fallbackToDestructiveMigration()
                    .build()

            val list = db.locationDao()!!.getAllLocations()
            db.close()

            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    art.animate().alpha(1f).start()
                    clear.isClickable = false
                    clear.isEnabled = false
                } else {
                    locationsAdapter.setList(list)
                    recyclerView.adapter = locationsAdapter
                    recyclerView.scheduleLayoutAnimation()
                }
            }
        }

        clear.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                db.locationDao()?.nukeTable()
                val list = db.locationDao()!!.getAllLocations()

                withContext(Dispatchers.Main) {
                    if (list.isEmpty()) {
                        locationsAdapter.clearList()
                        art.animate().alpha(1f).start()
                        clear.isClickable = false
                        clear.isEnabled = false
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        art.clearAnimation()
        recyclerView.clearAnimation()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onLocationItemClicked(locations: Locations) {
        locationAdapterCallback?.onLocationItemClicked(locations)
        dialog?.dismiss()
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val p0 = locationsAdapter.removeItem(viewHolder.absoluteAdapterPosition)

            CoroutineScope(Dispatchers.Default).launch {
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                db.locationDao()?.deleteLocation(p0)
                db.close()
            }
        }
    }
}
