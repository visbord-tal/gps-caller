package com.example.talvisbord.callgate;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Tal.Visbord on 12/22/2016.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    public static final String TAG = "GeofenceIntentService";

    // TODO: 12/22/2016 make phone editable
    private static final String GATE_PHOAN_NUMBER = "0525522078";

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error! " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.i(TAG, "Geofence Entered. Calling gate");
            makePhoneCall();
        } else {
            // Log the error.
            Log.e(TAG, "Geofence transition not supported. " + geofenceTransition);
        }
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + GATE_PHOAN_NUMBER));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "cant call gate. NO PHONE PERMISSIONS!");
            return;
        }
        getBaseContext().startActivity(intent);
    }
}
