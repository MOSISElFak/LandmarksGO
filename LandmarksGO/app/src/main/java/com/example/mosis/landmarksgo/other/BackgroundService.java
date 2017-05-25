package com.example.mosis.landmarksgo.other;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

//TODO: Implement LocationListener and upload user location to server
public class BackgroundService extends Service {
    private static final String TAG = "ServiceTAG";
    private static boolean serviceRunning;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"BackgroundService onCreate started");
        serviceRunning=true;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while(serviceRunning) {
                    synchronized (this) {
                        try {
                            wait(3000);
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(200);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        };
        Thread serviceThread = new Thread(r);
        serviceThread.start();
        Log.d(TAG,"BackgroundService onCreate ended");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"BackgroundService onStartCommand started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"BackgroundService onDestroy");
        serviceRunning=false;
        //serviceThread.stop();
        super.onDestroy();
    }
}
