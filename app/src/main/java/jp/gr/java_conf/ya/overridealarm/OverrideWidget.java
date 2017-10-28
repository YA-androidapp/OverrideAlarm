package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.NumberFormat;
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

    private static final String PREFS_NAME_LOC = "jp.gr.java_conf.ya.overridealarm.OverrideWidget.Location";
    private static final String PREF_PREFIX_KEY_PRE = "appwidget_pre_";

    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    static String loadLocPref(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_LOC, 0);
        String titleValue = prefs.getString(key + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "";
        }
    }

    static void saveLocPref(Context context, int appWidgetId, String key, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME_LOC, 0).edit();
        prefs.putString(key + appWidgetId, text);
        prefs.apply();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Location currentLocation) {
        if (currentLocation == null)
            return;

        Date today = new Date();

        for (int appWidgetId : appWidgetIds) {
            // 家までの距離を求める
            String textLat = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT);
            String textLon = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON);
            double homeLat = Double.parseDouble(textLat);
            double homeLon = Double.parseDouble(textLon);
            double currentLat = currentLocation.getLatitude();
            double currentLon = currentLocation.getLongitude();
            double currentDistance = CoordsUtil.calcDistHubeny(homeLat, currentLat, homeLon, currentLon);
            currentDistance = currentDistance > 40000000 ? 40000000 : currentDistance;

            CharSequence widgetText
                    = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT)
                    + "," + OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON)
                    + ":" + numberFormat.format(currentDistance);

            // 前回測位時の距離
            String textPreDistance = loadLocPref(context, appWidgetId, PREF_PREFIX_KEY_PRE);
            double preDistance = textPreDistance.equals("") ? 40000000 : Double.parseDouble(textPreDistance);

//            if (currentLocation.getSpeed() < 1) { // getSpeed()は、単位[m/sec]
//                // 歩くより遅い場合はアラート
//                Toast.makeText(context, R.string.alert_quiescence, Toast.LENGTH_LONG).show();
//                //TODO
//            } else
            if (currentDistance < preDistance) {
                // 前回測位時の距離より近づいていれば、記録を更新
                saveLocPref(context, appWidgetId, PREF_PREFIX_KEY_PRE, Double.toString(currentDistance));
            } else {
                // 前回測位時の距離より遠くなっていたら、アラート
                Toast.makeText(context, R.string.alert, Toast.LENGTH_LONG).show();

            }

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

