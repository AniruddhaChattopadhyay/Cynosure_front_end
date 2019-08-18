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

        Trie trie = new Trie();
        trie.insert("word");
        trie.insert("wojjj");
        trie.insert("jjjwo");
        trie.insert("lwojjj");
        array_of_words_that_begin_with = trie.autoComplete("wo");
        Log.d("KANISHKA",array_of_words_that_begin_with+"");


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
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
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
    }
}
