package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Test.SensorData;

public class GameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    private Map map;                                // Controls the map view
    private GoogleApiClient googleApiClient;        // Controls location tracking

    private LocationRequest locationRequest;        // Contains data used by the location listener
    private boolean isLocationTracking = false;     // Is the location listener tracking

    private String myId;                            // Player id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.activity_game);

        // Initialize SensorData
        SensorData.Initialize(this);
        SensorData.StartSensorTracking(SensorData.SensorFlag.PROXIMITY);

        // Create the googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)           // Connect onConnected(&Suspended) function
                .addOnConnectionFailedListener(this)    // Add onConnectionFailed function
                .addApi(LocationServices.API)
                .build();

        // Set the location tracker's settings
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request permissions before doing anything else
        requestPermissions();

        // Setup the map object
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = new Map(mapFragment);

        // Add players
        // (this data is normally recieved from main activity)
        myId = "player_1";
        Player[] players = {
                new Player(myId, "Player 1", new LatLng(0.0, 0.0))
        };

        for (Player player : players) {
            map.addMapItem(player);         // Add the players to the map object
        }
    }

    // Test Wall creation
    private boolean creatingWall = false;
    private Wall testWall;

    public void createWall(View view) {
        if (!creatingWall) {
            creatingWall = true;
            testWall = new Wall(
                    "test_wall", new LatLng[0], getString(R.string.google_maps_key_server)
            );
            map.addMapItem(testWall);
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText("Stop Creating Wall");
        }
        else {
            creatingWall = false;
            testWall = null;
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText("Create Wall");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Permission functionality
    // ---------------------------------------------------------------------------------------------

    /**
     * Request permissions at runtime (required for Android 6.0).
     */
    void requestPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // TODO: Show an explanation
            // } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION
                );
            // }
        } else {
            googleApiClient.connect();
        }
    }

    /**
     * Is called when the permission request is answered
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                // TODO: check if the permission was granted, and do something when it was not
                googleApiClient.connect();
                break;

            default:
                // Some other permission was granted
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Location Tracking functionality
    // ---------------------------------------------------------------------------------------------

    /**
     * Starts listening for location updates.
     */
    private void startLocationUpdate() {
        if(googleApiClient.isConnected() && !isLocationTracking) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this
            );
        }
        isLocationTracking = true;
    }

    /**
     * Stops listening for location updates.
     */
    private void stopLocationUpdate() {
        if(googleApiClient.isConnected() && isLocationTracking) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            isLocationTracking = false;
        }
    }

    /**
     * Is called when the application is paused
     */
    public void onPause() {
        super.onPause();
        SensorData.Pause();
        stopLocationUpdate();
    }

    /**
     * Is called when the application is resumed
     */
    public void onResume() {
        super.onResume();
        SensorData.Resume();
        if (googleApiClient.isConnected() && !isLocationTracking)
            startLocationUpdate();
    }

    /**
     * Is called when the googleApiClient is connected
     */
    @Override
    public void onConnected(Bundle hint) {
        startLocationUpdate();
    }

    /**
     * Is called when the googleApiClient is no longer connected
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Is called when the googleApiClient can't connect
     * @param connectionResult the result of the connection attempt
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Is called when the device's location is changed
     * @param location the new location of the device
     */
    @Override
    public void onLocationChanged(Location location) {
        // First check if creatingWall should be toggled
        try {
            int count = SensorData.ProximityCount();
            // If the count is divisible by 4, all toggles cancel
            if(count >= 2 && count % 4 != 0) {
                findViewById(R.id.wallButton).performClick();
                SensorData.ResetProximityCount();
            }
        } catch(Exception e) {
            // Proximity not available
        }

        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        map.updatePlayer(myId, loc);
        map.updateCamera(loc);

        // Test wall creation
        if (creatingWall) {
            testWall.addPoint(loc);
            map.redraw(testWall.getId());
        }
    }
}
