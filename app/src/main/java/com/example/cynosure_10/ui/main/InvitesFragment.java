package com.example.cynosure_10.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cynosure_10.R;
import com.example.cynosure_10.TravelGroups;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class InvitesFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static ArrayList<String> groups;
    private static Context context;
    private PageViewModel pageViewModel;
    private String grpName = "";

    public static InvitesFragment newInstance(int index, Context contextL, ArrayList<String> groupInvites) {
        context = contextL;
        InvitesFragment fragment = new InvitesFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        groups = groupInvites;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAB","INVITE");
        Log.d("TAB INFO", getArguments().getInt(ARG_SECTION_NUMBER)+"");

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    private String userLogin(){
        return "8777651851";
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final ArrayAdapter<String> listenadapter = new ArrayAdapter<>(context, R.layout.listdata , groups);
        View view = inflater.inflate(R.layout.invites,container,false);
        ListView listView = (ListView) view.findViewById(R.id.inviteList);
        listView.setAdapter(listenadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String groupName = groups.get(position);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("CUSTOMERS").document(userLogin());
                        Map<String,Object> updates = new HashMap<>();
                        updates.put("invites", FieldValue.arrayRemove(groupName));
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d("INVITE", "GROUP DELETED FROM INVITE");
                                    }
                                });
                                docRef = db.collection("GROUPS").document(groupName);
                                updates = new HashMap<>();
                                updates.put("Users", FieldValue.arrayUnion(userLogin()));
                                docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), " YOUR INVITATION IS ACCEPTED ", Toast.LENGTH_LONG).show();
                                        Log.d("INVITE", "GROUP ADDED TO CUSTOMER");
                                    }
                                });
                                listenadapter.remove(groupName);
                                groups.remove(groupName);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), " YOUR INVITATION IS REJECTED ", Toast.LENGTH_LONG).show();
                                    }
                                });
                                listenadapter.remove(groupName);
                                groups.remove(groupName);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to accept invitation from"+ groupName +" ?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        Button addgroups = (Button)view.findViewById(R.id.addGroup);
        addgroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("New Group");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                builder.setView(input);
                Log.d("FLOATING BUTTON", "CLICKED");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        grpName = input.getText().toString();
                        Log.d("GRP NAME", grpName);
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("GROUPS").document(grpName);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Toast.makeText(context, "GROUP NAME EXISTS! TRY ANOTHER", Toast.LENGTH_LONG).show();
                                    } else {
                                        Map<String, Object> docData = new HashMap<>();
                                        docData.put("Users", Arrays.asList(userLogin()));
                                        db.collection("GROUPS").document(grpName)
                                            .set(docData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("GROUP CREATION", "DocumentSnapshot successfully written!");
                                                        Toast.makeText(context, "GROUP CREATED! ", Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("GROUP CREATION", "Error writing document", e);
                                                    }
                                                });
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
        return view;
    }
}
