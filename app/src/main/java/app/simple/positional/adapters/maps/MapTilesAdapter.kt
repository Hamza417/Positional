package app.simple.positional.adapters.maps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.preferences.OSMPreferences
import app.simple.positional.util.ViewUtils.makeInvisible
import app.simple.positional.util.ViewUtils.makeVisible
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapTilesAdapter : RecyclerView.Adapter<MapTilesAdapter.Holder>() {

    private lateinit var mapTilesCallbacks: MapTilesCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_map_tiles, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].second

        if (list[position].second == OSMPreferences.getMapTileProvider()) {
            holder.indicator.makeVisible(false)
        } else {
            holder.indicator.makeInvisible(false)
        }

        holder.container.setOnClickListener {
            mapTilesCallbacks.onMapTilesSelected(list[position].second)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.adapter_tiles_tv)
        val indicator: ImageView = itemView.findViewById(R.id.tiles_indicator)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.tiles_adapter_item_container)
    }

    fun setOnMapTilesClickListener(mapTilesCallbacks: MapTilesCallbacks) {
        this.mapTilesCallbacks = mapTilesCallbacks
    }

    companion object {
        val list = mutableListOf(
                Pair(TileSourceFactory.DEFAULT_TILE_SOURCE, "Mapnik"),
                Pair(TileSourceFactory.BASE_OVERLAY_NL, "BASE_OVERLAY_NL"),
                // Pair(TileSourceFactory.CLOUDMADESMALLTILES, "CLOUDMADESMALLTILES"),
                // Pair(TileSourceFactory.CLOUDMADESTANDARDTILES, "CLOUDMADESTANDARDTILES"),
                Pair(TileSourceFactory.ChartbundleENRH, "ChartbundleENRH"),
                Pair(TileSourceFactory.ChartbundleENRL, "ChartbundleENRL"),
                Pair(TileSourceFactory.ChartbundleWAC, "ChartbundleWAC"),
                // Pair(TileSourceFactory.FIETS_OVERLAY_NL, "FIETS_OVERLAY_NL"),
                Pair(TileSourceFactory.HIKEBIKEMAP, "HIKEBIKEMAP"),
                // Pair(TileSourceFactory.OPEN_SEAMAP, "OPEN_SEAMAP"),
                Pair(TileSourceFactory.OpenTopo, "OpenTopo"),
                // Pair(TileSourceFactory.PUBLIC_TRANSPORT, "PUBLIC_TRANSPORT"),
                // Pair(TileSourceFactory.ROADS_OVERLAY_NL, "ROADS_OVERLAY_NL"),
                // Pair(TileSourceFactory.USGS_SAT, "USGS_SAT"),
                // Pair(TileSourceFactory.USGS_TOPO, "USGS_TOPO"),
                Pair(TileSourceFactory.WIKIMEDIA, "WIKIMEDIA")
        )

        interface MapTilesCallbacks {
            fun onMapTilesSelected(string: String)
        }
    }
}
