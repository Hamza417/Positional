package app.simple.positional.decorations.maps

import org.osmdroid.tileprovider.tilesource.CloudmadeTileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase

object OsmTileSource {
    // CloudMade tile sources are not in mTileSource because they are not free
    // and therefore not provided by default.
    val CLOUDMADESTANDARDTILES: OnlineTileSourceBase = CloudmadeTileSource(
            "CloudMadeStandardTiles", 0, 18, 256, ".png", arrayOf("http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
            "http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
            "http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s"))
}