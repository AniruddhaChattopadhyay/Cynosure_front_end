package com.example.cynosure_10;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;


    private static final int MY_PERMISSION_REQUEST = 7000;
    private static final int PLAY_SERVICE_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 300;
    private static int FASTEST_INTERVAL = 100;
    private static int DISPLACEMENT = 1;
    private int NOTIFICATION_ID = 1337;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;


    public MapsService() {

    }

    @Override
    public void onCreate() {
        startInForeground();

        drivers = FirebaseDatabase.getInstance().getReference("Test");
        geoFire = new GeoFire(drivers);

        String id = drivers.push().getKey();
        Log.d("KANISHKA","OnCreate");

        setupLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();
        Log.d("KANISHKA","Location Changed");

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

        }
        else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            return false;
        }
        return true;
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null){


            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            Log.d("KANISHKA","Location Service = "+ latitude + "   "+ longitude);
            //Updating to Firebase
            geoFire.setLocation("You", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }
        else
            Log.d("MAP","error");

    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i + (1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if(t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });

    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Notif")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TEST")
                .setContentText("HELLO")
                .setContentIntent(pendingIntent);
        Notification notification=builder.build();
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel("Notif", "Notification_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }

}
