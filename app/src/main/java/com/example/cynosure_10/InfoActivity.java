package com.example.cynosure_10;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class InfoActivity extends AppCompatActivity {

    private TextView bus_name, bus_number, bus_timings;
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

        bus_name.setText(Name);
        bus_number.setText(Number);
        bus_timings.setText(Timings);

    }
}
