package com.example.cynosure_10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class InfoActivity extends AppCompatActivity {

    private TextView bus_name, bus_number, bus_timings,bus_nearby;
    private String Name, Number, Timings;
    private LatLng Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Name =  getIntent().getStringExtra("NAME");
        Location =  getIntent().getExtras().getParcelable("POSITION");
        Number = getIntent().getStringExtra("NUMBER");
        Timings = "10AM - 10PM";
        Log.d("KANISHKA", "MainActivity:  " + Location.latitude + Name);

        bus_name = findViewById(R.id.bus_name);
        bus_number = findViewById(R.id.bus_number);
        bus_timings = findViewById(R.id.bus_timing);
        bus_nearby = findViewById(R.id.bus_nearby);

        bus_name.setText(Name);
        bus_number.setText(Number);
        bus_timings.setText(Timings);

        findViewById(R.id.route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, Route.class);
                intent.putExtra("BUS NAME", Name);
                startActivity(intent);
            }
        });

        bus_nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(InfoActivity.this, MapsActivity.class);
                intent.putExtra("FILTER",Name);
                startActivity(intent);

            }
        });

    }
}
