package app.simple.positional.decorations.views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import app.simple.positional.R;
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout;
import app.simple.positional.util.LocationExtension;
import app.simple.positional.util.StatusBarHeight;

public class MapToolbar extends DynamicCornerLinearLayout {
    
    private MapToolbarCallbacks mapToolbarCallbacks;
    private ImageButton location;
    private ImageButton menu;
    private boolean isFixed = false;
    
    public MapToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setProperties();
    }
    
    public MapToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setProperties();
    }
    
    private void setProperties() {
        initViews();
        setLayoutTransition(new LayoutTransition());
    }
    
    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_map_panel, this, true);
    
        setPadding(getResources().getDimensionPixelOffset(R.dimen.toolbar_padding),
                getResources().getDimensionPixelOffset(R.dimen.toolbar_padding) + StatusBarHeight.getStatusBarHeight(getResources()),
                getResources().getDimensionPixelOffset(R.dimen.toolbar_padding),
                getResources().getDimensionPixelOffset(R.dimen.toolbar_padding));
    
        location = view.findViewById(R.id.gps_location_indicator);
        menu = view.findViewById(R.id.gps_menu);
    
        location.setOnClickListener(it -> mapToolbarCallbacks.onLocationReset(it));
        location.setOnLongClickListener(v -> {
            mapToolbarCallbacks.onLocationLongPressed();
            return true;
        });
        menu.setOnClickListener(it -> mapToolbarCallbacks.onMenuClicked(it));
    }
    
    public void hide() {
        animate().translationY(getHeight() * -1).alpha(0).setInterpolator(new DecelerateInterpolator(1.5F)).start();
        location.setClickable(false);
        menu.setClickable(false);
        setClickable(false);
    }
    
    public void show() {
        animate().translationY(0).alpha(1).setInterpolator(new DecelerateInterpolator(1.5F)).start();
        location.setClickable(isFixed);
        menu.setClickable(true);
        setClickable(true);
    }
    
    public void locationIndicatorUpdate(boolean isFixed) {
        this.isFixed = isFixed;
        
        if (isFixed) {
            location.setImageResource(R.drawable.ic_gps_fixed);
            location.setClickable(true);
        }
        else {
            locationIconStatusUpdates();
        }
    }
    
    public void locationIconStatusUpdates() {
        if (LocationExtension.INSTANCE.getLocationStatus(getContext())) {
            location.setImageResource(R.drawable.ic_gps_not_fixed);
        }
        else {
            location.setImageResource(R.drawable.ic_gps_off);
            location.setClickable(false);
        }
    }
    
    public void setOnMapToolbarCallbacks(MapToolbarCallbacks mapToolbarCallbacks) {
        this.mapToolbarCallbacks = mapToolbarCallbacks;
    }
    
    public interface MapToolbarCallbacks {
        void onLocationReset(View view);
        
        void onLocationLongPressed();
        
        void onMenuClicked(View view);
    }
}
