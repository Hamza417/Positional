package app.simple.positional.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import app.simple.positional.services.ClockWidgetService;

public class ClockWidget extends AppWidgetProvider {
    
    private PendingIntent service;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent(context, ClockWidgetService.class);
        
        if (service == null) {
            service = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, service);
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, ClockWidgetService.class));
        super.onEnabled(context);
    }
}
