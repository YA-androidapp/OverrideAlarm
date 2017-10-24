package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class OverrideWidgetService extends Service implements LocationListener { // }, OnNmeaMessageListener {
    private static boolean gStarted = false;
    private float minDistance = 0; // 0km // 寝込んだ時用
    private long minTime = 0; // 5 * 60 * 1000; // 5min

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
        Log.d("overridealarm", "onStartCommand");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null)
            try {
                // locationManager.addNmeaListener(this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
                Log.d("overridealarm", "requestLocationUpdates");
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

//    @Override
//    public void onNmeaMessage(String message, long timestamp) {
//        Log.d("overridealarm", message);
//    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("overridealarm", "onLocationChanged");

        ComponentName widget = new ComponentName(this, OverrideWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);

        OverrideWidget.updateAppWidget(this, appWidgetManager, appWidgetIds, location);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
