package com.example.cynosure_10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class DiscoverActivity extends AppCompatActivity {
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    EndpointDiscoveryCallback endpointDiscoveryCallback;
    private String SERVICE_ID = "com.example.cynosure_10";
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        if (ContextCompat.checkSelfPermission(DiscoverActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DiscoverActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }
        else startDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private String getUserName(){
        return "Aadi";
    }

    private void startDiscovery() {
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
        endpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                        Log.d("DISCOVERED", info.getEndpointName());
                        Toast.makeText(DiscoverActivity.this, "Connected "+endpointId, Toast.LENGTH_LONG).show();
                        Nearby.getConnectionsClient(getApplicationContext())
                                .requestConnection(getUserName(), endpointId, connectionLifecycleCallback)
                                .addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // We successfully requested a connection. Now both sides
                                                // must accept before the connection is established.
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Nearby Connections failed to request the connection.
                                            }
                                        });
                    }

                    @Override
                    public void onEndpointLost(@NonNull String endpointId) {
                        Log.d("DISCONNECTED", endpointId);
                    }
                };
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("DISCOVER", "SUCCESS");
                                Toast.makeText(DiscoverActivity.this, "STARTED DISCOVERING", Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("DISCOVER", "FAILURE", e);
                            }
                        });
    }
}
