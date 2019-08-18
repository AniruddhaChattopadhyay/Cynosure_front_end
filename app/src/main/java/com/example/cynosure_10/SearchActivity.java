package com.example.cynosure_10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sql.StatementEvent;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    private FirebaseFirestore firestore;
    private List<String> suggest_list;
    private Collection suggestion_coll;
    private EditText searchbar;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        final Trie trie = new Trie();

        suggest_list = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listview);
        searchbar = (EditText) findViewById(R.id.search);

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("BUSES").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                            trie.insert(documentSnapshot.getId());
                            suggest_list.add(documentSnapshot.getId());
                            Log.d("KANISHKA", suggest_list+"");
                        }
                        adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1,suggest_list);
                        listView.setAdapter(adapter);
                    }
                });








        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                suggestion_coll = trie.autoComplete(searchbar.getText().toString());
//                if (suggestion_coll instanceof List)
//                    suggest_list = (List) suggestion_coll;
//                else
//                    suggest_list = new ArrayList(suggestion_coll);
//                Collections.sort(suggest_list);
//                Log.d("KANISHKA", suggest_list+"");
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1,suggest_list);
//                listView.setAdapter(adapter);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (SearchActivity.this).adapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
