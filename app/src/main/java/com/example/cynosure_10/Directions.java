package com.example.cynosure_10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Directions extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private String CITY = "KOLKATA";

    private AutoCompleteTextView source;
    private AutoCompleteTextView destination;

    private String[] stops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("STOPS").document(CITY).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        stops = (String[]) documentSnapshot.get("stops");
                        startfunction();
                    }
                });
    }

    private void startfunction(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, stops);
        source.setThreshold(1); //will start working from first character
        source.setAdapter(adapter);

        destination.setThreshold(1);
        destination.setAdapter(adapter);
    }
}
