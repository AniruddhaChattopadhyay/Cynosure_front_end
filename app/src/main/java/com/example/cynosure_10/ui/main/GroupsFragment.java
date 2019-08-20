package com.example.cynosure_10.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cynosure_10.GroupActivity;
import com.example.cynosure_10.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import io.paperdb.Paper;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static ArrayList<String> groups;
    private static Context context;
    private PageViewModel pageViewModel;

    public static GroupsFragment newInstance(int index, Context contextL, ArrayList<String> groupInvites) {
        GroupsFragment fragment = new GroupsFragment();
        groups = groupInvites;
        context = contextL;
        Paper.init(context);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    private String userLogin(){
        String a = Paper.book().read("PHONE");
        return a;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final ArrayAdapter<String> listenadapter;
        if (groups != null && groups.size() !=0)
            listenadapter = new ArrayAdapter<>(context, R.layout.listdata , groups);
        else
            listenadapter = new ArrayAdapter<>(context,R.layout.listdata);
        View view = inflater.inflate(R.layout.groups,container,false);
        final ListView listView = (ListView) view.findViewById(R.id.showGroups);
        listView.setAdapter(listenadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent groupActivity = new Intent(context, GroupActivity.class);
                groupActivity.putExtra("GroupName", ((TextView)view).getText());
                startActivity(groupActivity);
            }
        });
        return view;
    }
}