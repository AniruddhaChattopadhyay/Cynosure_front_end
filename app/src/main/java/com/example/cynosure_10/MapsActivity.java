package com.example.cynosure_10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST = 7000;
    private static final int PLAY_SERVICE_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 100;
    private static int FASTEST_INTERVAL = 10;
    private static int DISPLACEMENT = 1;

    private String BUS_NUMBER;

    private FirebaseFirestore firestore;

    private AutoCompleteTextView searchView;

    private String phone;
    private int count = 0;
    private ArrayList<String> buses;
    private List<Marker> markerArrayList = new ArrayList<>();
    private ArrayList<String> bus_list = new ArrayList<>();
    private ArrayList<String> bus_filtered = new ArrayList<>();
    private double temp_lat;
    private double temp_lon;
    private String temp_name;
    private Collection<String> suggestion_coll;
    private List<String> suggest_list;

    private MarkerOptions options = new MarkerOptions();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();


    DatabaseReference customer;
    DatabaseReference driver;
    GeoFire geoFire;

    Marker mCurrent;

    SupportMapFragment supportMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchView = (AutoCompleteTextView) findViewById(R.id.search);

        try {
            BUS_NUMBER = getIntent().getStringExtra("FILTER");
            Log.d("KANISHA_TEST",BUS_NUMBER);
        } catch (Exception e) {
            Log.d("KANISHKA", "No buses selecter. Showing all buses.");
        }


        bus_filtered = new ArrayList<>();



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,suggest_list);

        searchView.setAdapter(adapter);
        searchView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String bus_name = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(MapsActivity.this,InfoActivity.class);
                intent.putExtra("NAME", bus_name);
                if (mLastLocation != null)
                    intent.putExtra("POSITION",new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        buses = new ArrayList<>();


        Paper.init(this);
        phone = Paper.book().read("PHONE");

        Switch button =  findViewById(R.id.location_switch);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    startLocationUpdates();
                    displayLocation();


                }
            }
        });

        customer = FirebaseDatabase.getInstance().getReference("Customers");
        driver = FirebaseDatabase.getInstance().getReference("Drivers");
        geoFire = new GeoFire(customer);


        setupLocation();
    }



    @Override
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST);

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
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_REQUEST).show();
            }
            else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
            finish();
            }

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

            Log.d("KANISHKA","Location = "+ latitude + "   "+ longitude);
            //Updating to Firebase
            geoFire.setLocation(phone, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.d("KANISHKA","mCurrent");
                    if(mCurrent != null)
                        mCurrent.setPosition(new LatLng(latitude,longitude));
                    else {
                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude,longitude))
                            .title("You"));
                    mCurrent.setTag(0);
                    }
                    if (count==0)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));

                }
            });
            if (count%20==0) {

                if (BUS_NUMBER != null) {

                    firestore = FirebaseFirestore.getInstance();
                    firestore.collection("BUSES").document(BUS_NUMBER).collection("buses").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        bus_list.add(documentSnapshot.getId());
                                    }
                                }
                            });
                }

                driver.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                LatLng loc = new LatLng(snapshot.child("Location").child("l").child("0").getValue(Double.class), snapshot.child("Location").child("l").child("1").getValue(Double.class));
                                Location targetLocation = new Location("");
                                targetLocation.setLatitude(loc.latitude);
                                targetLocation.setLongitude(loc.longitude);
                                Log.d("KANISHKA", "test:  " + loc.latitude);

                                double distanceInKiloMeters = (mLastLocation.distanceTo(targetLocation)) / 1000; // as distance is in meter

                                if (distanceInKiloMeters <= 100) {
                                    // It is in range of 100 km
                                    buses.add(snapshot.getKey());
                                    Log.d("KANISHKA", "test:  " + snapshot.getKey());
                                }
                            }
                        } catch (Exception e){
                            Log.d("KANISHKA",e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            count++;

            if (buses.size()>0 && bus_list.size()>0 && bus_filtered.size() == 0){
                bus_filtered =  intersection(bus_list,buses);
                Log.d("KANISHKA_TEST", ""+bus_filtered);
            }
            else
                Log.d("KANISHKA_TEST",buses.size()+"  "+bus_list.size());


        }
        else
            Log.d("MAP","error");

    }

    public <T> ArrayList<T> intersection(ArrayList<T> list1, ArrayList<T> list2) {
        ArrayList<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST);

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);



//        Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        displaybuses();
        displayLocation();
        Log.d("KANISHKA","Location Changed");
    }

    private void displaybuses() {

        if (buses.size()>0){
            latlngs.clear();
            names.clear();
            ArrayList<String> bus;
            if (bus_filtered.size() != 0){
                bus = bus_filtered;
            }
            else {
                bus = buses;
            }
            Log.d("KANISHKA",markerArrayList.size() + "");
            for (int i = 0; i < bus.size(); i++) {
                String number = bus.get(i);
                final int x = i;
                Log.d("KANISHKA","Number:  "+number);
                driver.child(number).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        temp_lat = dataSnapshot.child("Location").child("l").child("0").getValue(Double.class);
                        temp_lon = dataSnapshot.child("Location").child("l").child("1").getValue(Double.class);
                        temp_name = dataSnapshot.child("Bus Name").getValue(String.class);
                        Log.d("KANISHKA","data:  "+temp_name);
                        Log.d("KANISHKA", "SIZE:  " + x + "   "+markerArrayList.size());
                        try{
                            markerArrayList.get(x).setPosition(new LatLng(temp_lat, temp_lon));
                        }catch (Exception e){
                            options.position(new LatLng(temp_lat,temp_lon));
                            options.title(temp_name);
                            options.snippet("someDesc");
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.transparent_marker));
                            markerArrayList.add(mMap.addMarker(options));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
//            for (int i=0;i<latlngs.size();i++) {
//                options.position(latlngs.get(i));
//                options.title(names.get(i));
//                options.snippet("someDesc");
//                Marker marker = mMap.addMarker(options);
//                marker.setTag(0);
//            }
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        displaybuses();
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
    public boolean onMarkerClick(Marker marker) {

        if (marker.getTag() == null)
            marker.setTag(0);
        if ( (Integer) marker.getTag()%2 == 0){
            int tag = (Integer) marker.getTag();
            tag = tag +1;
            marker.setTag(tag);
            Log.d("KANISHKA", "Marker Tag  :  " + tag);
        }
        else {
            int tag = (Integer) marker.getTag();
            tag = tag +1;
            marker.setTag(tag);
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra("NAME",marker.getTitle());
            intent.putExtra("POSITION",marker.getPosition());
            intent.putExtra("NUMBER", marker.getSnippet());
            startActivity(intent);}
        return false;
    }


}
