package app.simple.positional.dialogs.settings

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EdgeEffect
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.adapters.LocationsAdapter
import app.simple.positional.callbacks.LocationAdapterCallback
import app.simple.positional.database.LocationDatabase
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.Locations
import app.simple.positional.util.flingTranslationMagnitude
import app.simple.positional.util.forEachVisibleHolder
import app.simple.positional.util.overscrollRotationMagnitude
import app.simple.positional.util.overscrollTranslationMagnitude
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var clear: ImageButton
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

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        CoroutineScope(Dispatchers.Default).launch {
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
            CoroutineScope(Dispatchers.Default).launch {
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

        recyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                return object : EdgeEffect(recyclerView.context) {

                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        handlePull(deltaDistance)
                    }

                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        handlePull(deltaDistance)
                    }

                    private fun handlePull(deltaDistance: Float) {
                        // This is called on every touch event while the list is scrolled with a finger.
                        // We simply update the view properties without animation.
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        val rotationDelta = sign * deltaDistance * overscrollRotationMagnitude
                        val translationYDelta =
                                sign * recyclerView.width * deltaDistance * overscrollTranslationMagnitude
                        recyclerView.forEachVisibleHolder { holder: RecyclerView.ViewHolder ->
                            if (holder is LocationsAdapter.Holder) {
                                holder.rotation.cancel()
                                holder.translationY.cancel()
                                holder.itemView.rotation += rotationDelta
                                holder.itemView.translationY += translationYDelta
                            }
                        }
                    }

                    override fun onRelease() {
                        super.onRelease()
                        // The finger is lifted. This is when we should start the animations to bring
                        // the view property values back to their resting states.
                        recyclerView.forEachVisibleHolder { holder: RecyclerView.ViewHolder ->
                            if (holder is LocationsAdapter.Holder) {
                                holder.rotation.start()
                                holder.translationY.start()
                            }
                        }
                    }

                    override fun onAbsorb(velocity: Int) {
                        super.onAbsorb(velocity)
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        // The list has reached the edge on fling.
                        val translationVelocity = sign * velocity * flingTranslationMagnitude
                        recyclerView.forEachVisibleHolder { holder: RecyclerView.ViewHolder ->
                            if (holder is LocationsAdapter.Holder) {
                                holder.translationY
                                        .setStartVelocity(translationVelocity)
                                        .start()
                            }
                        }
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
            val p0 = locationsAdapter.removeItem(viewHolder.adapterPosition)

            CoroutineScope(Dispatchers.Default).launch {
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                db.locationDao()?.deleteLocation(p0)
                db.close()
            }
        }
    }
}
