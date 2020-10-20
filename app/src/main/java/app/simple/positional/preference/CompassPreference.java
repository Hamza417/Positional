package app.simple.positional.preference;

import android.content.Context;
import android.hardware.SensorManager;

import app.simple.positional.R;

public class CompassPreference {
    private String preferences = "Compass";
    
    // Parallax
    public void setParallax(boolean value, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean("parallax", value).apply();
    }
    
    public boolean getParallax(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean("parallax", false);
    }
    
    // Delay
    public int getDelay(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("delay", SensorManager.SENSOR_DELAY_GAME);
    }
    
    public void setDelay(int value, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("delay", value).apply();
    }
    
    // Compass Skins
    public void setSkins(int needle, int dial, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("needle", needle).apply();
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("dial", dial).apply();
    }
    
    public void setNeedle(int needle, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("needle", needle).apply();
    }
    
    public void setDial(int dial, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("dial", dial).apply();
    }
    
    public int getNeedle(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("needle", R.drawable.compass_needle_classic);
    }
    
    public void setLastNeedle(int dial, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("last_needle", dial).apply();
    }
    
    public int getLastNeedle(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("last_needle", R.drawable.compass_dial_minimal);
    }
    
    public int getDial(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("dial", R.drawable.compass_dial_minimal);
    }
    
    public void setLastDial(int dial, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("last_dial", dial).apply();
    }
    
    public int getLastDial(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("last_dial", R.drawable.compass_dial_minimal);
    }
    
    public int[] getSkins(Context context) {
        return new int[] {
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("needle", R.drawable.compass_needle_classic),
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("dial", R.drawable.compass_dial_minimal)};
    }
    
    // Dial Opacity
    public void setDialOpacity(float value, Context context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat("alpha", value).apply();
    }
    
    public float getDialOpacity(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat("alpha", 0.75f);
    }
    
    /**
     * Rotate which Dial or Needle
     *
     * @param value 1 for needle
     *              2 for dial
     *              3 for both
     *              Needle is the priority/default
     */
    public void setRotatePreference(Context context, int value) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("rotateWhich", value).apply();
    }
    
    public int getRotatePreference(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("rotateWhich", 1);
    }
    
    // Parallax Sensitivity
    public void setParallaxSensitivity(Context context, int value) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("sensitivity", value).apply();
    }
    
    public int getParallaxSensitivity(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("sensitivity", 1);
    }
    
    /**
     * @param value 1 for Accelerometer
     *              2 for Gyroscope
     *              2 is default, if not available 1 is used
     */
    public void setParallaxSensor(Context context, int value) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt("parallax_sensor", value).apply();
    }
    
    public int getParallaxSensor(Context context) {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt("parallax_sensor", 2);
    }
}
