package app.simple.positional.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.adapters.clock.TimeZoneAdapter
import app.simple.positional.decorations.searchview.SearchView
import app.simple.positional.decorations.views.CustomRecyclerView
import app.simple.positional.preferences.SearchPreferences
import app.simple.positional.util.LocaleHelper
import app.simple.positional.util.StatusBarHeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimeZones : ScopedFragment() {

    private lateinit var timeZoneAdapter: TimeZoneAdapter
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var searchView: SearchView

    private var timeZones: MutableList<Pair<String, String>> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_timezone_list, container, false)

        recyclerView = view.findViewById(R.id.timezones_rv)
        searchView = view.findViewById(R.id.search_view)

        val p0 = ZoneOffset.getAvailableZoneIds().toList()

        for (i in p0.indices) {
            timeZones.add(Pair(p0[i], getOffset(p0[i])))
        }

        val params = searchView.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin,
                StatusBarHeight.getStatusBarHeight(resources) + params.topMargin,
                params.rightMargin,
                params.bottomMargin)

        recyclerView.setPadding(recyclerView.paddingLeft,
                recyclerView.paddingTop + params.topMargin + params.height + params.bottomMargin,
                recyclerView.paddingRight,
                recyclerView.paddingBottom)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeZoneAdapter = TimeZoneAdapter(timeZones, SearchPreferences.getLastSearchKeyword())
        //recyclerView.scrollToPosition(ClockPreferences.getTimezoneSelectedPosition())

        searchView.setSearchViewEventListener { keywords, count ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                var filtered: MutableList<Pair<String, String>> = arrayListOf()

                if (count > 0) {
                    try {
                        for (str in timeZones) {
                            if (str.first.lowercase(Locale.getDefault()).contains(
                                    keywords.lowercase(
                                        Locale.getDefault()
                                    )
                                ) ||
                                str.second.lowercase(Locale.getDefault()).contains(
                                    keywords.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            ) {
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
                    timeZoneAdapter.timeZones = filtered
                    timeZoneAdapter.searchText = keywords
                    timeZoneAdapter.notifyDataSetChanged()
                }
            }
        }

        searchView.setKeywords(SearchPreferences.getLastSearchKeyword())
        recyclerView.adapter = timeZoneAdapter
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
