package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class OverrideBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName widget = new ComponentName(context, OverrideWidget.class);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(widget);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        OverrideWidget.updateAppWidget(context, appWidgetManager, appWidgetIds);
    }
}