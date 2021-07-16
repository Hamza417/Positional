package app.simple.positional.adapters.bottombar;

import android.content.Context;

import java.util.ArrayList;

import app.simple.positional.R;
import app.simple.positional.constants.LocationPins;
import app.simple.positional.preferences.BottomBarPreferences;
import app.simple.positional.preferences.GPSPreferences;

public class BottomBarItems {
    public static ArrayList<BottomBarModel> getBottomBarItems(Context context) {
        ArrayList<BottomBarModel> list = new ArrayList<>();

        if (BottomBarPreferences.INSTANCE.getClockPanelVisibility()) {
            list.add(new BottomBarModel(R.drawable.ic_clock, "clock", context.getString(R.string.clock)));
        }

        if (BottomBarPreferences.INSTANCE.getCompassPanelVisibility()) {
            list.add(new BottomBarModel(R.drawable.ic_compass, "compass", context.getString(R.string.compass)));
        }

        if (BottomBarPreferences.INSTANCE.getGpsPanelVisibility()) {
            list.add(new BottomBarModel(LocationPins.INSTANCE.getLocationsPins()[GPSPreferences.INSTANCE.getPinSkin()],
                    "location",
                    context.getString(R.string.gps_location)));
        }

        if (BottomBarPreferences.INSTANCE.getTrailPanelVisibility()) {
            list.add(new BottomBarModel(R.drawable.ic_trail, "trail", context.getString(R.string.trail)));
        }

        if (BottomBarPreferences.INSTANCE.getLevelPanelVisibility()) {
            list.add(new BottomBarModel(R.drawable.ic_level, "level", context.getString(R.string.level)));
        }

        if (BottomBarPreferences.INSTANCE.getSettingsPanelVisibility()) {
            list.add(new BottomBarModel(R.drawable.ic_settings, "settings", context.getString(R.string.settings)));
        }

        return list;
    }
}
