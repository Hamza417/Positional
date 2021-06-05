package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.TimeZoneAdapter
import app.simple.positional.callbacks.TimeZonesCallback
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimeZones : CustomDialogFragment(), TimeZonesCallback {

    lateinit var timeZoneAdapter: TimeZoneAdapter
    private var timeZones: MutableList<Pair<String, String>> = mutableListOf()

    private var animateCount = 1

    fun newInstance(): TimeZones {
        return this
    }

    private lateinit var timezonesRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var nothingFound: ImageView
    private lateinit var currentItemSubstring: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_timezone_list, container, false)

        timezonesRecyclerView = view.findViewById(R.id.timezones_rv)
        searchEditText = view.findViewById(R.id.search_timezone)
        currentItemSubstring = view.findViewById(R.id.current_item_substring)
        nothingFound = view.findViewById(R.id.nothing_found)

        val p0 = ZoneOffset.getAvailableZoneIds().toList()

        for (i in p0.indices) {
            timeZones.add(Pair(p0[i], getOffset(p0[i])))
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeZoneAdapter = TimeZoneAdapter(timeZones, this as TimeZonesCallback, "")
        timezonesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        timezonesRecyclerView.adapter = timeZoneAdapter
        timezonesRecyclerView.scrollToPosition(ClockPreferences.getTimezoneSelectedPosition())

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CoroutineScope(Dispatchers.Default).launch {
                    var filtered: MutableList<Pair<String, String>> = arrayListOf()

                    if (count > 0) {
                        try {
                            for (str in timeZones) {
                                if (str.first.toLowerCase(Locale.getDefault()).contains(s.toString().toLowerCase(Locale.getDefault())) ||
                                        str.second.toLowerCase(Locale.getDefault()).contains(s.toString().toLowerCase(Locale.getDefault()))) {
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

                    filtered.sortBy { it.first }

                    withContext(Dispatchers.Main) {
                        if (filtered.size == 0) {
                            if (animateCount == 1) {
                                nothingFound.animate().scaleY(1.2f).scaleX(1.2f).alpha(1.0f).setInterpolator(DecelerateInterpolator()).start()
                                animateCount = 0
                            }
                        } else {
                            if (animateCount == 0) {
                                animateCount = 1
                                nothingFound.animate().scaleY(1.0f).scaleX(1.0f).alpha(0f).setInterpolator(DecelerateInterpolator()).start()
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

        timezonesRecyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
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
        currentItemSubstring.text = p0
    }

    override fun itemClicked(p1: String) {
        ClockPreferences.setTimeZone(p1)
        this.dialog?.dismiss()
    }

    override fun onPause() {
        super.onPause()
        nothingFound.clearAnimation()
    }

    private fun getOffset(timezone: String): String {
        val zoneId = ZoneId.of(timezone)
        val zonedDateTime: ZonedDateTime = Instant.now().atZone(zoneId)
        return zonedDateTime.format(DateTimeFormatter.ofPattern("XXX").withLocale(LocaleHelper.getAppLocale())).replace("Z", "+00:00")
    }

    companion object {
        fun newInstance(): TimeZones {
            val args = Bundle()
            val fragment = TimeZones()
            fragment.arguments = args
            return fragment
        }
    }
}
