package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.Double2;
import android.util.Log;

public class OverrideWidgetService extends Service implements LocationListener {
    private static boolean gStarted = false;
    private float minDistance = 1000; // 1km
    private long minTime = 5 * 60 * 1000; // 5min
    private static final String PREFS_NAME_LOC = "jp.gr.java_conf.ya.overridealarm.OverrideWidget.Location";
    private static final String PREF_PREFIX_KEY_PRE = "appwidget_pre_";

    private LocationManager locationManager;

    public static boolean isStarted() {
        return gStarted;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null)
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            } catch (SecurityException e) {
            }

        gStarted = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (locationManager != null)
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
            }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("overridealarm", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("overridealarm", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("overridealarm", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // location.getLatitude()
        // location.getLongitude()
        // location.getSpeed()

        ComponentName widget = new ComponentName(this, OverrideWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
        boolean[] distances = new boolean[appWidgetIds.length];

        int i = 0;
        for (int appWidgetId : appWidgetIds) {
            // 家までの距離を求める
            String textLat = OverrideWidgetConfigureActivity.loadTitlePref(this, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LAT);
            String textLon = OverrideWidgetConfigureActivity.loadTitlePref(this, appWidgetId, OverrideWidgetConfigureActivity.PREF_PREFIX_KEY_LON);
            double homeLat = Double.parseDouble(textLat);
            double homeLon = Double.parseDouble(textLon);
            double currentLat = location.getLatitude();
            double currentLon = location.getLongitude();
            double currentDistance = CoordsUtil.calcDistHubeny(homeLat, currentLat, homeLon, currentLon);

            // 前回測位時の距離
            String textPreDistance = loadLocPref(this, appWidgetId, PREF_PREFIX_KEY_PRE);
            double preDistance = Double.parseDouble(textPreDistance);

            if(currentDistance>preDistance) {
                distances[i] = true;
                saveLocPref(this, appWidgetId, PREF_PREFIX_KEY_PRE, Double.toString(currentDistance));
            }else{
                distances[i] = false;
            }

            i++;
        }

        OverrideWidget.updateAppWidget(this, appWidgetManager, appWidgetIds, distances);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

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
}
