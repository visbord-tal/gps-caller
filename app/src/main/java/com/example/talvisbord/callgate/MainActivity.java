package com.example.talvisbord.callgate;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.function.ToDoubleBiFunction;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    public static final String TAG = "MainActivity";

    // TODO: 12/22/2016 support multiple fences and prompt the user to edit these constants.
    private static final float GEOFENCE_RADIUS_IN_METERS = 100F;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 0;
    private static final String GEO_KEY = "MY_FIRST_TARGET";
    private static final double GEOFENCE_LONG = 34.8943128;
    private static final double GEOFENCE_LAT = 32.0651486;



    GoogleApiClient mGoogleApiClient;
    Geofence mGeofence;
    private PendingIntent mGeofencePendingIntent;

    private View mRootView;
    private View mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.activity_main);
        mRootView = findViewById(R.id.root);
        mProgressBar = findViewById(R.id.progressBar);
        findViewById(R.id.create_target_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateGeoClicked();
            }
        });

    }

    private void onCreateGeoClicked() {
        // TODO: 12/22/2016 get accurate location
//        Location location = getLastKnownLocation();
//        if (location != null) {
            Geofence geofence = createGeofence(GEO_KEY, GEOFENCE_LAT, GEOFENCE_LONG);
            registerGeofence(geofence);
//        } else {
//            Log.e(TAG, "FAILED TO GET LOCATION..");
//        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private boolean checkPermissions() {
        // TODO: 12/22/2016 add phone call permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting Loacation Permissions: ");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return false;
        }
        return true;
    }



    private void registerGeofence(Geofence geofence) {
        Log.d(TAG, "registering Geofence: " + geofence.toString());
        if(checkPermissions()){
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofence(geofence);
            GeofencingRequest geofencingRequest = builder.build();

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    geofencingRequest,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
    }

    Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // TODO: 12/22/2016 turn GPS ON
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "got Last Location: accuracy=" + lastLocation.getAccuracy() + ", long=" + lastLocation.getLongitude() + ", lat=" + lastLocation.getLatitude());
        return lastLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "location Permission granted");
            // TODO: 12/22/2016
        } else {
            Log.d(TAG, "location Permission NOT granted ");
            Snackbar.make(mRootView, R.string.permission_denied, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private Geofence createGeofence(String key, double latitude, double longitude) {
            //TODO well and responsiveness
            mGeofence = new Geofence.Builder()
                .setRequestId(key)

                .setCircularRegion(
                        latitude,
                        longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        return mGeofence;
    }

    private void removeGeofence() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    protected void onStart() {
        Log.d(TAG, "onStart: Connecting to google client");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        Log.d(TAG, "onStop: Disconnecting from google client");
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "on Client Connected: ");
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "on Client Connection Suspended: ");
        // TODO: 12/22/2016  
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "on Client Connection Suspended: ");
        // TODO: 12/22/2016          
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG, "on geofence Result: status = " + status);
        if(status.isSuccess()){
            Snackbar.make(mRootView, R.string.target_created_success, Snackbar.LENGTH_INDEFINITE).show();
        }
        else{
            Snackbar.make(mRootView, R.string.target_created_error, Snackbar.LENGTH_INDEFINITE).show();
        }

    }

}
