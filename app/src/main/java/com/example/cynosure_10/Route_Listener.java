package com.example.cynosure_10;

import android.graphics.Color;
import android.util.Log;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class Route_Listener implements RoutingListener {

    private GoogleMap mmap;
    private LatLng start, end;
    private List<LatLng> way;
    private ArrayList<Polyline> polylines;
    final int[] colorList = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLACK, Color.DKGRAY, Color.YELLOW, Color.GRAY};


    public Route_Listener(GoogleMap mmap,List<LatLng> waypoints) {
        this.mmap = mmap;
        this.way = waypoints;
        this.start = way.get(0);
        this.end = way.get(way.size()-1);
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(way)
                .key("AIzaSyDL7zpLLBhXnQdTltFBJ0bwzphysGcZiEE")
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestrouteindex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mmap.moveCamera(center);

        try {
            if (polylines.size() > 0) {
                for (Polyline poly : polylines) {
                    poly.remove();
                }
            }
        } catch (Exception e){
            Log.d("LOCATION", e.toString());
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % colorList.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(colorList[colorIndex]);
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mmap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(MapsActivity.this,"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        mmap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        mmap.addMarker(options);

        mmap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 17.0f));

    }

    @Override
    public void onRoutingCancelled() {

    }
}
