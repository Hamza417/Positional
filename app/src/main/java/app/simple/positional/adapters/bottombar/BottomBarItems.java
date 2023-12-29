package app.simple.positional.adapters.bottombar;

import android.content.Context;

import java.util.ArrayList;

import app.simple.positional.R;
import app.simple.positional.constants.LocationPins;

public class BottomBarItems {

    public static final String CLOCK = "clock";
    public static final String COMPASS = "compass";
    public static final String DIRECTION = "direction";
    public static final String LOCATION = "location";
    public static final String TRAIL = "trail";
    public static final String LEVEL = "level";
    public static final String SETTINGS = "settings";

    public static ArrayList<BottomBarModel> getBottomBarItems(Context context) {
        ArrayList<BottomBarModel> list = new ArrayList<>();

        list.add(new BottomBarModel(R.drawable.ic_clock, CLOCK, context.getString(R.string.clock)));
        list.add(new BottomBarModel(R.drawable.ic_compass, COMPASS, context.getString(R.string.compass)));
        list.add(new BottomBarModel(R.drawable.ic_near_me, DIRECTION, context.getString(R.string.direction)));
        list.add(new BottomBarModel(LocationPins.INSTANCE.getLocationPin(), LOCATION, context.getString(R.string.gps_location)));
        list.add(new BottomBarModel(R.drawable.ic_trail_line, TRAIL, context.getString(R.string.trail)));
        list.add(new BottomBarModel(R.drawable.ic_level, LEVEL, context.getString(R.string.level)));
        list.add(new BottomBarModel(R.drawable.ic_settings, SETTINGS, context.getString(R.string.settings)));

        return list;
    }
}
