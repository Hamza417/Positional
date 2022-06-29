package app.simple.positional.adapters.bottombar;

import android.content.Context;

import java.util.ArrayList;

import app.simple.positional.R;

public class BottomBarItems {
    public static ArrayList<BottomBarModel> getBottomBarItems(Context context) {
        ArrayList<BottomBarModel> list = new ArrayList<>();

        list.add(new BottomBarModel(R.drawable.ic_clock, "clock", context.getString(R.string.clock)));
        list.add(new BottomBarModel(R.drawable.ic_compass, "compass", context.getString(R.string.compass)));
        list.add(new BottomBarModel(R.drawable.ic_near_me, "direction", context.getString(R.string.direction)));
        list.add(new BottomBarModel(R.drawable.ic_place_notification, "location", context.getString(R.string.gps_location)));
        list.add(new BottomBarModel(R.drawable.ic_trail_line, "trail", context.getString(R.string.trail)));
        list.add(new BottomBarModel(R.drawable.ic_level, "level", context.getString(R.string.level)));
        list.add(new BottomBarModel(R.drawable.ic_settings, "settings", context.getString(R.string.settings)));

        return list;
    }
}
