package cwa115.trongame.Map;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores information (e.g. item locations) of the map.
 */
public class Map implements OnMapReadyCallback {
    // Store the map fragment to draw on
    private MapFragment mapFragment;

    // Store the items currently in the game
    private HashMap<String, DrawableMapItem> mapItems;

    // Store the items that have to be redrawn on the map
    private ArrayList<DrawableMapItem> pendingItems;

    /**
     * Class initializer
     * @param _mapFragment the map fragment object used to draw on
     */
    public Map(MapFragment _mapFragment) {
        mapItems = new HashMap<>();
        pendingItems = new ArrayList<>();

        mapFragment = _mapFragment;
    }

    public void addMapItem(DrawableMapItem item) {
        mapItems.put(item.getId(), item);
    }

    /**
     * Update a player map item
     * @param id the player identifier (i.e. username)
     * @param location player location, expressed as a (latitude, longitude) pair
     */
    public void updatePlayer(String id, LatLng location) {
        Player player = (Player)mapItems.get(id);
        player.setLocation(location);
        redraw(player);
    }

    public void redraw(DrawableMapItem[] items) {
        for (DrawableMapItem item : items) {
            pendingItems.add(item);
        }
        mapFragment.getMapAsync(this);
    }

    public void redraw(DrawableMapItem item) {
        pendingItems.add(item);
        mapFragment.getMapAsync(this);
    }

    /**
     * Activates when the GoogleMap object is ready for use (getMapAsync finishes)
     * @param map note: cannot be stored since it will be destroyed when this callback exits
     */
    @Override
    public void onMapReady(GoogleMap map) {
        for(DrawableMapItem item : pendingItems) {
            item.draw(map);
        };
    }
}
