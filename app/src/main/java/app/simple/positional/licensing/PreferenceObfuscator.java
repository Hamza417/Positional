package app.simple.positional.licensing;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * An wrapper for SharedPreferences that transparently performs data obfuscation.
 */
public class PreferenceObfuscator {
    
    private static final String TAG = "PreferenceObfuscator";
    
    private final SharedPreferences mPreferences;
    private final Obfuscator mObfuscator;
    private SharedPreferences.Editor mEditor;
    
    /**
     * Constructor.
     *
     * @param sp A SharedPreferences instance provided by the system.
     * @param o  The Obfuscator to use when reading or writing data.
     */
    public PreferenceObfuscator(SharedPreferences sp, Obfuscator o) {
        mPreferences = sp;
        mObfuscator = o;
        mEditor = null;
    }
    
    @SuppressLint ("CommitPrefEdits")
    public void putString(String key, String value) {
        if (mEditor == null) {
            mEditor = mPreferences.edit();
        }
        String obfuscatedValue = mObfuscator.obfuscate(value, key);
        mEditor.putString(key, obfuscatedValue);
    }
    
    public String getString(String key, String defValue) {
        String result;
        String value = mPreferences.getString(key, null);
        if (value != null) {
            try {
                result = mObfuscator.unobfuscate(value, key);
            } catch (ValidationException e) {
                // Unable to unobfuscate, data corrupt or tampered
                Log.w(TAG, "Validation error while reading preference: " + key);
                result = defValue;
            }
        }
        else {
            // Preference not found
            result = defValue;
        }
        return result;
    }
    
    public void commit() {
        if (mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}
