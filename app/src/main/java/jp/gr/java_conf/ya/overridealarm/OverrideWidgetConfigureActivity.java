package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The configuration screen for the {@link OverrideWidget OverrideWidget} AppWidget.
 */
public class OverrideWidgetConfigureActivity extends Activity {

    private static final int REQUEST_PERMISSION = 1234;
    private static final String PREFS_NAME = "jp.gr.java_conf.ya.overridealarm.OverrideWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    static final String PREF_PREFIX_KEY_LAT = PREF_PREFIX_KEY + "lat_";
    static final String PREF_PREFIX_KEY_LON = PREF_PREFIX_KEY + "lon_";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mAppWidgetTextLat;
    private EditText mAppWidgetTextLon;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = OverrideWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetTextLat = mAppWidgetTextLat.getText().toString();
            String widgetTextLon = mAppWidgetTextLon.getText().toString();
            saveTitlePref(context, mAppWidgetId, PREF_PREFIX_KEY_LAT, widgetTextLat);
            saveTitlePref(context, mAppWidgetId, PREF_PREFIX_KEY_LON, widgetTextLon);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            OverrideWidget.updateAppWidget(context, appWidgetManager, new int[]{mAppWidgetId}, new boolean[]{false});

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public OverrideWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String key, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(key + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(key + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "";
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.override_widget_configure);
        mAppWidgetTextLat = (EditText) findViewById(R.id.appwidget_text_lat);
        mAppWidgetTextLon = (EditText) findViewById(R.id.appwidget_text_lon);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String textLat=loadTitlePref(OverrideWidgetConfigureActivity.this, mAppWidgetId, PREF_PREFIX_KEY_LAT);
        String textLon=loadTitlePref(OverrideWidgetConfigureActivity.this, mAppWidgetId, PREF_PREFIX_KEY_LON);
        if (!textLat.equals(""))
            mAppWidgetTextLat.setText(textLat);
        if (!textLon.equals(""))
            mAppWidgetTextLon.setText(textLon);

        checkGpsPermission();
    }

    private void checkGpsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);

            return;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Toast.makeText(this, R.string.denied_gps_permission, Toast.LENGTH_LONG).show();
            }
        }
    }
}

