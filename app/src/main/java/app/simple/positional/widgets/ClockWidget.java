package app.simple.positional.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import app.simple.positional.services.ClockWidgetService;

public class ClockWidget extends AppWidgetProvider {
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, getService(context));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    @Override
    public void onEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, ClockWidgetService.class));
        }
        else {
            context.startService(new Intent(context, ClockWidgetService.class));
        }
    
        super.onEnabled(context);
    }
    
    @Override
    public void onDisabled(Context context) {
        cancelWidgetProcess(context);
        super.onDisabled(context);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        cancelWidgetProcess(context);
        super.onDeleted(context, appWidgetIds);
    }
    
    private void cancelWidgetProcess(Context context) {
        try {
            context.stopService(new Intent(context, ClockWidgetService.class));
        } catch (IllegalStateException ignored) {
        }
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getService(context));
    }
    
    private PendingIntent getService(Context context) {
        return PendingIntent.getService(context,
                1543,
                new Intent(context, ClockWidgetService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
