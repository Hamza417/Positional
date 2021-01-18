package app.simple.positional.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

import app.simple.positional.preference.MainPreferences;

public class LocaleHelper extends ContextWrapper {
    
    public LocaleHelper(Context base) {
        super(base);
    }
    
    public static Context wrap(Context context) {
        String language = MainPreferences.INSTANCE.getAppLanguage();
        
        if (!language.equals("default")) {
            Configuration config = context.getResources().getConfiguration();
            if (!language.equals("")) {
                Locale locale = new Locale(language);
                Locale.setDefault(locale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale);
                }
                else {
                    setSystemLocaleLegacy(context, config, locale);
                }
                config.setLayoutDirection(locale);
                context = context.createConfigurationContext(config);
            }
            return new LocaleHelper(context);
        }
        return context;
    }
    
    public static String getSystemLanguage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getSystemLocale(context).getLanguage().toLowerCase();
        }
        else {
            return getSystemLocaleLegacy(context).getLanguage().toLowerCase();
        }
    }
    
    public static Locale getSystemLocaleLegacy(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.locale;
    }
    
    @TargetApi (Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Context context) {
        return context.getResources().getConfiguration().getLocales().get(0);
    }
    
    public static void setSystemLocaleLegacy(Context context, Configuration config, Locale locale) {
        config.locale = locale;
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        res.updateConfiguration(config, dm);
    }
    
    @TargetApi (Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }
}
