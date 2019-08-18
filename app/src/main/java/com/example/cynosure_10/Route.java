package com.example.cynosure_10;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import com.google.android.libraries.places.api.net.PlacesClient;

public class Route extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bus_name;
    private Marker searchmarker;
    private final String apiKey = "AIzaSyDL7zpLLBhXnQdTltFBJ0bwzphysGcZiEE";

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Places.initialize(this, apiKey);
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.f_auto_complete);

// Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d("KANISHKA",place.getName()+""+place.getLatLng());
                try {
                    MarkerOptions options = new MarkerOptions();
                    options.position(place.getLatLng());
                    options.title(place.getName());
                    mMap.addMarker(options);
                }catch (Exception e){
                    Log.d("KANISHKA",e.getMessage());
                }
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bus_name = getIntent().getStringExtra("BUS NAME");

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






        firestore = FirebaseFirestore.getInstance();

        firestore.collection("BUSES").document(bus_name).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            List<GeoPoint> route = (List<GeoPoint>) documentSnapshot.getData().get("Route");
                            List<LatLng> markerPoints = new ArrayList<>();

                            for (int i = 0; i < route.size(); i++) {
                                final int x = i;
                                LatLng temp;
                                temp = new LatLng(route.get(x).getLatitude(), route.get(x).getLongitude());
                                markerPoints.add(temp);
                            }


                            new Route_Listener(mMap, markerPoints);
                        }
                        catch (Exception e){
                            Log.d("KANISHKA",e.getMessage());
                        }
                    }
                });




    }


}

