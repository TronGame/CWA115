package cwa115.trongame;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    // The map storage object. This stores all the item and player information on the map
    private Map map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.activity_game);

        // Get the players
        Player[] players = {
                new Player("player_1", "Player 1", new LatLng(0.0, 0.0))
        };

        // Initialize the map storage object
        map = new Map(players);

        // Activate the map fragment defined in the content_game.xml layout
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    public void onProviderEnabled(String str) {


    }

    @Override
    public void onProviderDisabled(String provider) {


    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

}
