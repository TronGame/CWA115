package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    // The map storage object. This stores all the item and player information on the map
    private Map map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private String myId;
    private boolean isLocationTracking = false;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.activity_game);

        myId = "player_1";
        // Get the players
        Player[] players = {
                new Player(myId, "Player 1", new LatLng(0.0, 0.0))
        };

        // Initialize the map storage object
        map = new Map(players);

        // Store a reference to the map fragment defined in the content_game.xml layout
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request permissions before doing anything else
        requestPermissions();
    }

    /**
     * Request permissions at runtime (required for Android 6.0).
     */
    void requestPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // TODO: Show an explanation
            // } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION
                );
            // }
        }
    }

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

    public void onPause() {
        super.onPause();
        stopLocationUpdate();
    }

    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected() && !isLocationTracking)
            startLocationUpdate();
    }

    /**
     * Activates when the GoogleMap object is ready for use (getMapAsync finishes)
     * @param map_fragment note: cannot be stored since it will be destroyed when this callback exits
     */
    @Override
    public void onMapReady(GoogleMap map_fragment) {
        map.draw(map_fragment);
    }

    @Override
    public void onConnected(Bundle hint) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Player location
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        map.updatePlayer(myId, loc);
        mapFragment.getMapAsync(this);
    }
}
