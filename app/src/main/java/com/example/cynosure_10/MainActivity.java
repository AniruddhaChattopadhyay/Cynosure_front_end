package com.example.cynosure_10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    Collection<String> array_of_words_that_begin_with;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button mapButton = (Button)findViewById(R.id.mapsactivity);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

                Intent intent1 = new Intent(MainActivity.this, MapsService.class);
                startService(intent1);
            }
        });

        ((Button)findViewById(R.id.signboard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Directions.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.travelGroups)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TravelGroups.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.startAdvertise)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdvertiseActivity.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.startDiscover)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DiscoverActivity.class);
                startActivity(intent);
            }
        });
    }
}
