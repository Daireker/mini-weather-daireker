package com.example.daireker.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by daireker on 2017/12/8.
 */

public class MyService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent update = new Intent();
                update.setAction("UPDATE_WEATHER");
                sendBroadcast(update);
            }
        },0,300 * 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
