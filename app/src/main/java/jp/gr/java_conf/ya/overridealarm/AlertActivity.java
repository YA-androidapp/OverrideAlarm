package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA<ya.androidapp@gmail.com> All rights reserved.

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

public class AlertActivity extends AppCompatActivity {
    private MediaPlayer mp;
    private Switch switch_stop;
    private Vibrator vib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switch_stop = (Switch) findViewById(R.id.switch_stop);
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        switch_stop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    stopMp();
                    vibStop();
                } else {
                    //TODO
                }
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String url = sp.getString("notifications_new_message_ringtone", "");
        playMp(url);

        vibStart();

    }

    private void playMp(String url) {
        if (!url.equals("")) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                mp = MediaPlayer.create(this, uri);
                mp.setLooping(true);
                mp.seekTo(0);
                mp.start();
            }
        }
    }

    private void stopMp() {
        if (mp != null) {
            mp.stop();
            try {
                mp.prepare();
            } catch (Exception e) {
            }
        }
    }

    private void vibStart() {
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vib != null) {
            long pattern[] = {10, 100, 10, 100, 10, 100, 10, 100, 10, 100};
            vib.vibrate(pattern, -1);
        }
    }

    private void vibStop() {
        vib.cancel();
    }

}
