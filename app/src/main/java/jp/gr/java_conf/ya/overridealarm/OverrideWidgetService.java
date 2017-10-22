package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OverrideWidgetService extends Service {
    private static boolean gStarted = false;

    public static boolean isStarted() {
        return gStarted;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OverrideWidget.setAlarm(this, true);
        gStarted = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
