package app.simple.positional.adapters.bottombar;

import android.content.Context;

import java.util.ArrayList;

import app.simple.positional.R;
import app.simple.positional.constants.LocationPins;
import app.simple.positional.preferences.GPSPreferences;

public class BottomBarItems {
    public static ArrayList <BottomBarModel> getBottomBarItems(Context context) {
        ArrayList<BottomBarModel> list = new ArrayList<>();

        list.add(new BottomBarModel(R.drawable.ic_clock, "clock"));
        list.add(new BottomBarModel(R.drawable.ic_compass, "compass"));
        list.add(new BottomBarModel(LocationPins.INSTANCE.getLocationsPins()[GPSPreferences.INSTANCE.getPinSkin()], "location"));
        list.add(new BottomBarModel(R.drawable.ic_trail, "trail"));
        list.add(new BottomBarModel(R.drawable.ic_level, "level"));
        list.add(new BottomBarModel(R.drawable.ic_settings, "settings"));

        return list;
    }
}
