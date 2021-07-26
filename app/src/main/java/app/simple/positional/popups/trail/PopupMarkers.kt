package app.simple.positional.popups.trail

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import app.simple.positional.R
import app.simple.positional.adapters.trail.AdapterPopupMarkers
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.views.CustomRecyclerView

class PopupMarkers(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupMarkersCallbacks: PopupMarkersCallbacks

    init {
        init(contentView, view)

        val markers = contentView.findViewById<CustomRecyclerView>(R.id.markers_recycler_view)

        markers.layoutManager = GridLayoutManager(contentView.context, 3, GridLayoutManager.VERTICAL, false)

        val adapter = AdapterPopupMarkers()

        adapter.onIconClicked = {
            popupMarkersCallbacks.onMarkerClicked(it).also {
                dismiss()
            }
        }

        adapter.onIconLongClicked = {
            popupMarkersCallbacks.onMarkerLongClicked(it).also {
                dismiss()
            }
        }

        markers.adapter = adapter
    }

    fun setOnPopupMarkersCallbackListener(popupMarkersCallbacks: PopupMarkersCallbacks) {
        this.popupMarkersCallbacks = popupMarkersCallbacks
    }

    companion object {
        interface PopupMarkersCallbacks {
            fun onMarkerClicked(position: Int)
            fun onMarkerLongClicked(position: Int)
        }
    }
}
