package com.example.cynosure_10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity {
    private ArrayList<String> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        final String grpName = (String) getIntent().getStringExtra("GroupName");
        Log.d("GRP ACTIVIT", grpName);
        groupMembers = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CUSTOMERS").whereArrayContains("Groups", grpName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DATA", document.getId() + " => " + document.getData());
                                groupMembers.add((String) document.getData().get("name"));
                            }
                            createList();
                        } else {
                            Log.d("DATA", "Error getting documents: ", task.getException());
                        }
                    }
                });

        final Button addMember = (Button)findViewById(R.id.addMember);
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setTitle("New Member Enter Phone Number");
                final EditText input = new EditText(GroupActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String memberName = input.getText().toString();
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("GROUPS").document(grpName);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (!document.exists()) {
                                        Toast.makeText(GroupActivity.this, "INVALID GROUP NAME",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.d("MEMBER NAME", memberName);
                                        if(((ArrayList<String>)document.get("Users")).contains(memberName))
                                            Toast.makeText(GroupActivity.this, "ALREADY IN GROUP", Toast.LENGTH_SHORT).show();
                                        else
                                            addMember(memberName, grpName);
                                    }
                                } else {
                                    Log.d("EXCEPTION", "Failed with: ", task.getException());
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private String userLogin(){
        return "8777651851";
    }

    private void createList(){
        final ArrayAdapter<String> listenadapter = new ArrayAdapter<String>(GroupActivity.this, R.layout.listdata , groupMembers);
        final ListView listView = (ListView)findViewById(R.id.groupMembers);
        listView.setAdapter(listenadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //MAPS ACTIVITY
            }
        });
    }

    private void addMember(final String number, final String grpName){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("CUSTOMERS").document(number);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> numbers = (ArrayList<String>) document.get("invites");
                        if(numbers.contains(grpName)){
                            Toast.makeText(GroupActivity.this, "INVITE PREVIOUSLY SENT", Toast.LENGTH_LONG).show();
                        }else{
                            docRef.update("invites", FieldValue.arrayUnion(grpName))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(GroupActivity.this, "INVITE SUCCESSFULLY SENT", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(GroupActivity.this, "WRONG NUMBER ENTERED", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("EXCEPTION", "Failed with: ", task.getException());
                }
            }
        });
    }
}
