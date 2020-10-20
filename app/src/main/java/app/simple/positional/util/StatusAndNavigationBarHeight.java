package app.simple.positional.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Window;

import app.simple.positional.R;

public class StatusAndNavigationBarHeight {
    public static int getStatusBarHeight(Window window) {
        Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top - window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }
    
    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static int getNavigationBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    public static int getToolBarHeight(Context context, Resources resources) {
        // Calculate ActionBar height
        int i = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            i = TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }
        
        return i;
    }
}
