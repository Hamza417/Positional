package app.simple.positional.adapters.bottombar;

import android.content.Context;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import app.simple.positional.R;
import app.simple.positional.constants.LocationPins;
import app.simple.positional.model.BottomBar;

public class BottomBarItems {

    public static final String CLOCK = "clock";
    public static final String COMPASS = "compass";
    public static final String DIRECTION = "direction";
    public static final String LOCATION = "location";
    public static final String TRAIL = "trail";
    public static final String MEASURE = "measure";
    public static final String LEVEL = "level";
    public static final String SETTINGS = "settings";

    public static ArrayList<BottomBar> getBottomBarItems(Context context) {
        ArrayList<BottomBar> list = new ArrayList<>();

        list.add(new BottomBar(R.drawable.ic_clock, CLOCK, context.getString(R.string.clock), ContextCompat.getColor(context, R.color.time_bb_color)));
        list.add(new BottomBar(R.drawable.ic_compass, COMPASS, context.getString(R.string.compass), ContextCompat.getColor(context, R.color.compass_bb_color)));
        list.add(new BottomBar(R.drawable.ic_near_me, DIRECTION, context.getString(R.string.direction), ContextCompat.getColor(context, R.color.direction_bb_color)));
        list.add(new BottomBar(LocationPins.INSTANCE.getLocationPin(), LOCATION, context.getString(R.string.gps_location), ContextCompat.getColor(context, R.color.location_bb_color)));
        list.add(new BottomBar(R.drawable.ic_trail_line, TRAIL, context.getString(R.string.trail), ContextCompat.getColor(context, R.color.trail_bb_color)));
        list.add(new BottomBar(R.drawable.ic_square_foot, MEASURE, context.getString(R.string.measure), ContextCompat.getColor(context, R.color.measure_bb_color)));
        list.add(new BottomBar(R.drawable.ic_level, LEVEL, context.getString(R.string.level), ContextCompat.getColor(context, R.color.level_bb_color)));
        list.add(new BottomBar(R.drawable.ic_settings, SETTINGS, context.getString(R.string.settings), ContextCompat.getColor(context, R.color.settings_bb_color)));

        return list;
    }


}
