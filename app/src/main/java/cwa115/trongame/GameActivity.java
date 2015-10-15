package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GameActivity extends AppCompatActivity
    implements OnMapReadyCallback {

    // The map storage object. This stores all the item and player information on the map
    private Map map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.activity_game);

        // Initialize the map storage object
        map = new Map();

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

}
