package app.simple.positional.tileprovider;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class OsmTileProvider extends UrlTileProvider {

    public static String url = "https://tile.thunderforest.com/cycle/{z}/{x}/{y}.png";

    public OsmTileProvider(int width, int height) {
        super(width, height);
    }

    @Nullable
    @Override
    public URL getTileUrl(int i, int i1, int i2) {
        try {
            return new URL(url.replace("{z}", "" + i2).replace("{x}", "" + i).replace("{y}", "" + i1));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
