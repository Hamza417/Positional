package app.simple.positional.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import app.simple.positional.services.ClockWidgetService4x4;

public class ClockWidget4x4 extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        startService(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, getService(context));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        startService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelWidgetProcess(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        cancelWidgetProcess(context);
    }

    private void cancelWidgetProcess(Context context) {
        try {
            context.stopService(new Intent(context, ClockWidgetService4x4.class));
        } catch (IllegalStateException ignored) {
        }
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getService(context));
    }

    private void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, ClockWidgetService4x4.class));
        } else {
            context.startService(new Intent(context, ClockWidgetService4x4.class));
        }
    }

    private PendingIntent getService(Context context) {
        return PendingIntent.getService(context,
                1544,
                new Intent(context, ClockWidgetService4x4.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
