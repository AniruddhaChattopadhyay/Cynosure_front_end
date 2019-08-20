package com.example.cynosure_10;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class RegisterActivity extends AppCompatActivity {



    private Button CreateAccountBtn;
    private EditText Input_name,Input_phone, Input_psswd;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CreateAccountBtn = (Button) findViewById(R.id.resgister_btn);
        Input_name = (EditText) findViewById(R.id.register_username);
        Input_phone = (EditText) findViewById(R.id.register_phone);
        Input_psswd = (EditText) findViewById(R.id.register_password);
        loadingBar = new ProgressDialog(this);

        CreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateAccount_func();
            }
        });
    }

    private void CreateAccount_func()
    {
        String name = Input_name.getText().toString();
        String phone = Input_phone.getText().toString();
        String psswd = Input_psswd.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this,"Please enter the Bus Name",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(RegisterActivity.this,"Please enter your Phone Number",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(psswd)){
            Toast.makeText(RegisterActivity.this,"Please enter your password",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait while we validate");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidatephoneNumber(name,phone,psswd);
        }
    }

    private void ValidatephoneNumber(final String name, final String phone, final String psswd){

        final DatabaseReference RootRef;
        final FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        final CollectionReference collection = firestore.collection("CUSTOMERS");

        collection.document(phone).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                Toast.makeText(RegisterActivity.this, "This Bus Number already exists", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Toast.makeText(RegisterActivity.this, "Please try again with another Number", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Map<String, Object> data = new HashMap<>();
                                data.put("name", name);
                                data.put("Phone", phone);
                                data.put("password",psswd) ;

                                collection.document(phone)
                                        .set(data)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this, "Your account was created successfully", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                    Intent intent = new Intent(RegisterActivity.this, Login_Activity.class);
                                                    startActivity(intent);
                                                }
                                                else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RegisterActivity.this, task.getException().getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });


    }
}
