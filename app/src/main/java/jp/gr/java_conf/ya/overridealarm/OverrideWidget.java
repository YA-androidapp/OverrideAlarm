package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static java.lang.Math.round;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverrideWidgetConfigureActivity OverrideWidgetConfigureActivity}
 */
public class OverrideWidget extends AppWidgetProvider {
    private static final HashMap<Integer, Boolean> haveArrived = new HashMap<>();

    private static final DecimalFormat dfDec = new DecimalFormat("0.##");
    private static final DecimalFormat dfInt = new DecimalFormat("#####");

    private static float revDistance = 2 * 1000; // 2km // 遠ざかった時の閾値

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.JAPAN);

    public static final String BUTTON_CLICK_ACTION = "BUTTON_CLICK_ACTION";
    private static final String PREF_PREFIX_KEY_PRE = "appwidget_pre_";
    private static final String PREFS_NAME_LOC = "jp.gr.java_conf.ya.overridealarm.OverrideWidget.Location";

    static String loadLocPref(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_LOC, 0);
        String titleValue = prefs.getString(key + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "";
        }
    }

    static void runAlert(Context context) {
        Intent i = new Intent(context.getApplicationContext(), AlertActivity.class);
        context.startActivity(i);
    }

    static void saveLocPref(Context context, int appWidgetId, String key, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME_LOC, 0).edit();
        prefs.putString(key + appWidgetId, text);
        prefs.apply();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Location currentLocation) {
        Log.d("overridealarm", "updateAppWidget");
        Date today = new Date();

        for (int appWidgetId : appWidgetIds) {
            Log.d("overridealarm", "updateAppWidget " + Integer.toString(appWidgetId));

            try {
                if(!haveArrived.get(appWidgetId))
                    haveArrived.put(appWidgetId, false);
            }catch(Exception e){
                haveArrived.put(appWidgetId, false);
            }

            CharSequence widgetText = "";
            double currentDistance = 40000000;

            if (currentLocation != null) {
                // 家までの距離を求める
                String textLat = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT);
                String textLon = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON);
                double homeLat = Double.parseDouble(textLat.equals("")?"0":textLat);
                double homeLon = Double.parseDouble(textLon.equals("")?"0":textLon);
                double currentLat = currentLocation.getLatitude();
                double currentLon = currentLocation.getLongitude();
                currentDistance = CoordsUtil.calcDistHubeny(homeLat, homeLon, currentLat, currentLon);
                currentDistance = currentDistance > 40000000 ? 40000000 : currentDistance;
                String currentDistanceKm = ((1000 > currentDistance) ? dfInt.format(round(currentDistance / 1000)) : dfDec.format(currentDistance / 1000)) + "km";

                String messageArrived = "";
                if (currentDistance < 1000) {
                    haveArrived.put(appWidgetId, true);
                    messageArrived = " " + context.getString(R.string.message_arrived);
                } else if (haveArrived.get(appWidgetId)){
                    messageArrived = " " + context.getString(R.string.message_havearrived);
                }

                widgetText
                        = OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT)
                        + "," + OverrideWidgetConfigureActivity.loadTitlePref(context, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON)
                        + ":" + currentDistanceKm + messageArrived;
            }

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.override_widget);
            views.setTextViewText(R.id.appwidget_button, widgetText + " " + sdf.format(today));

            // OnClick event
            Intent intent = new Intent(context.getApplicationContext(), OverrideWidgetService.class);
            intent.setAction(BUTTON_CLICK_ACTION);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);
            Log.d("overridealarm", "setOnClickPendingIntent");

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d("overridealarm", "updateAppWidget");

            if (currentLocation != null) {
                // 前回測位時の距離
                String textPreDistance = loadLocPref(context, appWidgetId, PREF_PREFIX_KEY_PRE);
                double preDistance = textPreDistance.equals("") ? 40000000 : Double.parseDouble(textPreDistance);

                if (haveArrived.get(appWidgetId)) {
                    // 家に着いたらリセット
                    saveLocPref(context, appWidgetId, PREF_PREFIX_KEY_PRE, "40000000");
                // } else if (currentLocation.getSpeed() < 1) { // getSpeed()は、単位[m/sec]
                    //TODO: debug時は静止しているので
                    // 歩くより遅い場合はアラート
                    // runAlert(context);
                } else if (currentDistance < preDistance) {
                    // 前回測位時の距離より近づいていれば、記録を更新
                    saveLocPref(context, appWidgetId, PREF_PREFIX_KEY_PRE, Double.toString(currentDistance));
                } else if (currentDistance - preDistance > revDistance) {
                    // 最接近時の距離から閾値より遠くなっていたら、アラート
                    runAlert(context);
                }
            }
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

