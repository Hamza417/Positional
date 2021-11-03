package app.simple.positional.popups.trail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import app.simple.positional.R
import app.simple.positional.adapters.trail.AdapterPopupMarkers
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.util.ViewUtils

class PopupMarkers(view: View, x: Float, y: Float) : PopupWindow() {

    private lateinit var popupMarkersCallbacks: PopupMarkersCallbacks

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_trail_markers,
                DynamicCornerLinearLayout(view.context))

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        contentView.clipToOutline = false
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 50F
        overlapAnchor = true

        ViewUtils.addShadow(contentView)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }

        setContentView(contentView)

        val markers = contentView.findViewById<CustomRecyclerView>(R.id.markers_recycler_view)

        markers.layoutManager =
            GridLayoutManager(contentView.context, 3, GridLayoutManager.VERTICAL, false)

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

        showAsDropDown(view, x.toInt(), y.toInt(), Gravity.NO_GRAVITY)
        ViewUtils.dimBehind(contentView)
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
