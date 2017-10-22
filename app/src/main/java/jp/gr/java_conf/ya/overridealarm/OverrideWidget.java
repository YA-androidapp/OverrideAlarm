package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverrideWidgetConfigureActivity OverrideWidgetConfigureActivity}
 */
public class OverrideWidget extends AppWidgetProvider {
    private static final String BUTTON_CLICK_ACTION = "jp.gr.java_conf.ya.overridealarm.BUTTON_CLICK_ACTION";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.JAPAN);

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, boolean[] flags) {
        Date today = new Date();

        int i = 0;
        for (int appWidgetId : appWidgetIds) {
            CharSequence widgetText
                    = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT)
                    + "," + OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON)
                    + "," + (flags[i]?"t":"f");

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.override_widget);
            views.setTextViewText(R.id.appwidget_button, widgetText + " " + sdf.format(today));

            // OnClick event
            Intent intent = new Intent(context, OverrideWidgetService.class);
            intent.setAction(BUTTON_CLICK_ACTION);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);
            Log.d("overridealarm", "setOnClickPendingIntent");

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (!OverrideWidgetService.isStarted())
            context.startService(new Intent(context, OverrideWidgetService.class));
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

