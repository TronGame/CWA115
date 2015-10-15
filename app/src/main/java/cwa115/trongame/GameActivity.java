package cwa115.trongame;

import android.location.Location;
import android.os.Bundle;
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

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // The map storage object. This stores all the item and player information on the map
    private Map map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private String myId;
    private boolean isLocationTracking = true;

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

        // Activate the map fragment defined in the content_game.xml layout
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Starts listening for location updates.
     */
    private void startLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this
        );
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
     * Activates when the google map object has started up and is ready to be controlled
     */
    @Override
    public void onMapReady(GoogleMap map_fragment) {
        // Store the GoogleMap object in the map storage object.
        map.setMap(map_fragment);
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
    }
}
