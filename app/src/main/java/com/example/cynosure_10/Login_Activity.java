package com.example.cynosure_10;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import io.paperdb.Paper;

public class Login_Activity extends AppCompatActivity {

    private Button Login_Btn;
    private EditText Phone,Input_Psswd,Bus_Name;
    private TextView Register;
    private ProgressDialog loadingbar;
    private String parentDbName = "CUSTOMERS";

    private CheckBox chkb_remember_me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        Login_Btn = (Button) findViewById(R.id.login_btn);
        Phone = (EditText) findViewById(R.id.login_phone);
        Input_Psswd = (EditText) findViewById(R.id.login_password);
        Register = (TextView) findViewById(R.id.register_text);
        loadingbar = new ProgressDialog(this);
        chkb_remember_me = (CheckBox) findViewById(R.id.remember_me_chkb);

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_Activity.this, RegisterActivity.class));
            }
        });

        Paper.init(this);

        if (Paper.book().read("PASSWORD") != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        Login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login_Function();
            }
        });
    }

    private void Login_Function()
    {
        String bus_number = Phone.getText().toString();
        String password = Input_Psswd.getText().toString();

        if(TextUtils.isEmpty(bus_number))
        {
            Toast.makeText(Login_Activity.this, "Please enter your Phone Number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(Login_Activity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Logging in");
            loadingbar.setMessage("Verifying Credentials");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            Account_Acess(bus_number,password);
        }

    }

    private void Account_Acess(final String phone, final String password)
    {
        final DatabaseReference RootRef;
        final FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        try {
            firestore.collection(parentDbName).document(phone).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            try {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    String actual_psswd = document.getData().get("password").toString();
                                    String name = document.getData().get("name").toString();
                                    if (actual_psswd.trim().equals(password.trim())) {
                                        Paper.book().write("PHONE", phone);
                                        Paper.book().write("NAME", name);
                                        if (chkb_remember_me.isChecked())
                                            Paper.book().write("PASSWORD", actual_psswd);
                                        Toast.makeText(Login_Activity.this, "Logging in", Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                        Intent intent = new Intent(Login_Activity.this, MapsActivity.class);
                                        startActivity(intent);
                                    } else {
                                        loadingbar.dismiss();
                                        Toast.makeText(Login_Activity.this, "Wrong Password/Phone Number", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    loadingbar.dismiss();
                                    Toast.makeText(Login_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e){
                                Toast.makeText(Login_Activity.this, "There seems to be a problem.Please recheck the credentials.", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Toast.makeText(Login_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            } catch (Exception e) {
            loadingbar.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        /**RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String care = dataSnapshot.child(parentDbName).child(phone).child("Caregiver").getValue(String.class);
                String name = dataSnapshot.child(parentDbName).child(phone).child("name").getValue(String.class);
                Paper.book().write(Prevalent.careGiver,care);
                Paper.book().write(Prevalent.name,name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Paper.book().write(Prevalent.userPhone,phone);
        if (chkb_remember_me.isChecked())
        {
            Paper.book().write(Prevalent.userPassword,password);
        }



        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(parentDbName).child(phone).exists())
                {
                    Users userData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if(userData.getPhone().equals(phone))
                    {
                        if (userData.getPassword().equals(password))
                        {
                            String userCaregiver = Paper.book().read(Prevalent.careGiver);
                            if (userCaregiver == "")
                                Paper.book().write(Prevalent.careGiver,userData.getCaregiver());

                            Toast.makeText(Login_Activity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();

                            //Intent intent1 = new Intent(Login_Activity.this, Accelerometer_data.class);
                            //startService(intent1);

                            Intent intent = new Intent(Login_Activity.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(Login_Activity.this, "Password is incorrect.", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                            Toast.makeText(Login_Activity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(Login_Activity.this, "Account with this Phnoe Number is not Registered.", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                    Toast.makeText(Login_Activity.this, "Please retry valid Login Credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });**/
    }
}
