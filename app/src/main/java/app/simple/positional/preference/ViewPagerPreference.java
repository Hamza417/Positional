package app.simple.positional.preference;

import android.content.Context;

public class ViewPagerPreference {
    protected String preferences = "Pager";
    
    public void setCurrentPager(Context context, int value) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("current_pager", value).apply();
    }
    
    public int getCurrentViewPager(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("current_pager", 1);
    }
}
