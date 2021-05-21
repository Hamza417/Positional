package app.simple.positional.decorations.bottombar;

import android.content.Context;

import java.util.ArrayList;

import app.simple.positional.R;

public class BottomBarItems {
    public static ArrayList <BottomBarModel> getBottomBarItems(Context context) {
        ArrayList <BottomBarModel> list = new ArrayList <>();
        
        list.add(new BottomBarModel(R.drawable.ic_clock, context.getString(R.string.clock)));
        list.add(new BottomBarModel(R.drawable.ic_compass, context.getString(R.string.compass)));
        list.add(new BottomBarModel(R.drawable.ic_pin_01, context.getString(R.string.gps_location)));
        list.add(new BottomBarModel(R.drawable.ic_level, context.getString(R.string.level)));
        //list.add(new BottomBarModel(R.drawable.ic_flashlight, context.getString(R.string.torch)));
        list.add(new BottomBarModel(R.drawable.ic_settings, context.getString(R.string.settings)));
        
        return list;
    }
}
