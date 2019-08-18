package com.example.cynosure_10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class AdvertiseActivity extends AppCompatActivity {

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private String SERVICE_ID = "com.example.cynosure_10";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);
        startAdvertising();
    }

    private String getBusName(){
        return "WB26C7875";
    }

    private void startAdvertising() {
        connectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                        Log.d("CONNECTION ENDPOINT", endpointId);
                    }

                    @Override
                    public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                Log.d("ACCEPTED", endpointId);
                                break;
                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                Log.d("REJECTED", endpointId);
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                Log.d("ERROR", endpointId);
                                break;
                            default:
                                // Unknown status code
                        }
                    }

                    @Override
                    public void onDisconnected(@NonNull String endpointId) {
                        Log.d("DISCONNECTED", endpointId);
                    }
                };
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        getBusName(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("ADVERTISING", "SUCCESS");
                                Toast.makeText(AdvertiseActivity.this, "STARTED ADVERTISING", Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("ADVERTISING", "FAILURE", e);
                            }
                        });
    }
}
