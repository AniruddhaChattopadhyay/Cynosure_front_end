package com.example.cynosure_10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class Directions extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String CITY = "KOLKATA";
    private String PHONE;

    private AutoCompleteTextView source_field;
    private AutoCompleteTextView destination_field;

    private Button search_btn;

    private ArrayList<String> stops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);


        Paper.init(this);
        PHONE = Paper.book().read("PHONE");

        source_field = findViewById(R.id.source_text);
        destination_field = findViewById(R.id.dest_text);
        search_btn = findViewById(R.id.search_btn);

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("STOPS").document(CITY).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            stops = (ArrayList<String>) documentSnapshot.get("stops");
                        }catch (Exception e){
                            Log.d("KANISHKA", e.getMessage().toString());
                            stops = new ArrayList<String>();
                        }

                        startfunction();
                    }
                });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDirections();
            }
        });

    }

    private void startfunction(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, stops);
        source_field.setThreshold(1); //will start working from first character
        source_field.setAdapter(adapter);

        destination_field.setThreshold(1);
        destination_field.setAdapter(adapter);

    }

    private void getDirections(){
        String source = source_field.getText().toString();
        String dest = destination_field.getText().toString();

        if (source.isEmpty() || dest.isEmpty() || source.equals(dest)){
            Toast.makeText(this, "Enter the Source and Destination properly.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("route_query");

        databaseReference.child(PHONE).setValue(source+":"+dest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(new myTask(), 10, TimeUnit.MILLISECONDS);
                }
        });

    }

    private class myTask implements Runnable{

        @Override
        public void run() {

            databaseReference = firebaseDatabase.getReference("route_result");
            databaseReference.child(PHONE)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getValue();
                           if (data!= null) {
                               showList(data);
                               dataSnapshot.getRef().removeValue();
                           }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


        }
    }

    private void showList(HashMap<String,String> data){
        ArrayList<SpannableString> listData = new ArrayList<>();


        for (String key : data.keySet()){
            String value = data.get(key);

            int length = key.length();

            value.replaceAll(" "," , ");
            Log.d("KANISHKA", value);

            key = (String) TextUtils.concat(key,"\n",value);

            SpannableString key_spannable = new SpannableString(key);

            key_spannable.setSpan(new StyleSpan(Typeface.BOLD),0,length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);



            listData.add(key_spannable);

        }

        ArrayAdapter<SpannableString> adapter = new ArrayAdapter<SpannableString>(Directions.this, android.R.layout.simple_list_item_1,listData);
        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

}
