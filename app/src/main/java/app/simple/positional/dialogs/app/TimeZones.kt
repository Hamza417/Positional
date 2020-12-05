package app.simple.positional.dialogs.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.TimeZoneAdapter
import app.simple.positional.callbacks.TimeZoneSelected
import app.simple.positional.callbacks.TimeZonesCallback
import app.simple.positional.util.flingTranslationMagnitude
import app.simple.positional.util.forEachVisibleHolder
import app.simple.positional.util.overscrollRotationMagnitude
import app.simple.positional.util.overscrollTranslationMagnitude
import app.simple.positional.views.CustomDialogFragment
import kotlinx.android.synthetic.main.dialog_timezone_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TimeZones : CustomDialogFragment(), TimeZonesCallback {

    lateinit var timeZoneSelected: TimeZoneSelected
    lateinit var timeZoneAdapter: TimeZoneAdapter
    private var timeZones: MutableList<String> = TimeZone.getAvailableIDs().toList() as MutableList<String>

    private var animateCount = 1

    fun newInstance(): TimeZones {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_timezone_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeZoneAdapter = TimeZoneAdapter(timeZones, this as TimeZonesCallback, "")
        timezones_rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        timezones_rv.adapter = timeZoneAdapter

        search_timezone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CoroutineScope(Dispatchers.Default).launch {
                    var filtered: MutableList<String> = arrayListOf()

                    if (count > 0) {
                        try {
                            for (str in timeZones) {
                                if (str.toLowerCase(Locale.getDefault()).contains(s.toString().toLowerCase(Locale.getDefault()))) {
                                    filtered.add(str)
                                }
                            }
                        } catch (ignored: ConcurrentModificationException) {
                        } catch (ignored: IndexOutOfBoundsException) {
                        } catch (ignored: NullPointerException) {
                        }
                    } else {
                        filtered = timeZones
                    }

                    filtered.sort()

                    withContext(Dispatchers.Main) {
                        if (filtered.size == 0) {
                            if (animateCount == 1) {
                                nothing_found.animate().scaleY(1.2f).scaleX(1.2f).alpha(1.0f).setInterpolator(DecelerateInterpolator()).start()
                                animateCount = 0
                            }
                        } else {
                            if (animateCount == 0) {
                                animateCount = 1
                                nothing_found.animate().scaleY(1.0f).scaleX(1.0f).alpha(0f).setInterpolator(DecelerateInterpolator()).start()
                            }
                        }
                        timeZoneAdapter.timeZones = filtered
                        timeZoneAdapter.searchText = s.toString()
                        timeZoneAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        timezones_rv.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
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
                            if (holder is TimeZoneAdapter.Holder) {
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
                            if (holder is TimeZoneAdapter.Holder) {
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
                            if (holder is TimeZoneAdapter.Holder) {
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

    override fun itemSubstring(p0: String) {
        current_item_substring.text = p0
    }

    override fun itemClicked(p1: String) {
        timeZoneSelected.onTimeZoneSelected(p1)
        this.dialog?.dismiss()
    }

    override fun onPause() {
        super.onPause()
        nothing_found.clearAnimation()
    }
}