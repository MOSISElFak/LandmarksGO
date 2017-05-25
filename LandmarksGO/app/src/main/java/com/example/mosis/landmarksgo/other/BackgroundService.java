package com.example.mosis.landmarksgo.other;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//TODO: Implement LocationListener and upload user location to server
public class BackgroundService extends Service implements LocationListener {
    private static final String TAG = "ServiceTAG";
    private static boolean serviceRunning;
    private LocationManager locationManager;
    private String provider;

    private int settingsGpsRefreshTime;
    private String loggedUserUid;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "BackgroundService onCreate started");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Log.d(TAG,"Location provider is selected: " + provider);

        Log.d(TAG,"BackgroundService onCreate ended");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"BackgroundService onStartCommand started");
        settingsGpsRefreshTime = intent.getIntExtra("settingsGpsRefreshTime",1);
        loggedUserUid = intent.getStringExtra("loggedUserUid");

        locationManager.requestLocationUpdates(provider, settingsGpsRefreshTime*1000, 0, this); //Actual time to get a new location is a little big higher- 3s instead of 1, 6s instead 5, 12s instead 10
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return 0;
        }else{
            //Location location = locationManager.getLastKnownLocation(provider);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"BackgroundService onDestroy");
        locationManager.removeUpdates(this);
        serviceRunning=false;
        //serviceThread.stop();
        super.onDestroy();
    }

    //>LOCATION
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"New location: " + location.getLatitude() + " " + location.getLongitude());
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        users.child(loggedUserUid).child("lat").setValue(location.getLatitude());
        users.child(loggedUserUid).child("lon").setValue(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG,"Location provider is enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG,"Location provider is disabled: " + provider);
    }
}
