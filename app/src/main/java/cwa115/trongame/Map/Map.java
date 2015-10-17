package cwa115.trongame.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores information (e.g. item locations) of the map
 * and controls the map view that shows the items
 */
public class Map implements OnMapReadyCallback {

    private MapFragment mapFragment;                    // MapFragment used to draw on
    private HashMap<String, DrawableMapItem> mapItems;  // Stores the items currently on the map
    private ArrayList<String> pendingItems;    // The items that still need to be redrawn

    /**
     * Class initializer
     * @param _mapFragment the map fragment object used to draw on
     */
    public Map(MapFragment _mapFragment) {
        mapFragment = _mapFragment;
        mapItems = new HashMap<>();
        pendingItems = new ArrayList<>();
    }

    /**
     * Add a map item to draw on the map
     * @param item the item that needs to be added
     */
    public void addMapItem(DrawableMapItem item) {
        mapItems.put(item.getId(), item);
        redraw(item.getId());
    }

    /**
     * Update a player map item
     * @param id the player identifier (i.e. username)
     * @param location player location, expressed as a (latitude, longitude) pair
     */
    public void updatePlayer(String id, LatLng location) {
        Player player = (Player)mapItems.get(id);
        player.setLocation(location);
        redraw(id);
    }

    /**
     * Redraw a set of items
     * @param itemIds item ids to redraw
     */
    public void redraw(String[] itemIds) {
        for (String itemId : itemIds) {
            DrawableMapItem item = mapItems.get(itemId);
            if (item != null) { pendingItems.add(itemId); }
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Redraw a set of items
     * @param itemId item id to redraw
     */
    public void redraw(String itemId) {
        DrawableMapItem item = mapItems.get(itemId);
        if (item != null) { pendingItems.add(itemId); }

        mapFragment.getMapAsync(this);
    }

    /**
     * Activates when the GoogleMap object is ready for use (getMapAsync finishes)
     * @param map note: cannot be stored since it will be destroyed when this callback exits
     */
    @Override
    public void onMapReady(GoogleMap map) {
        for(String itemId : pendingItems) {
            DrawableMapItem item = mapItems.get(itemId);
            if (item != null) { item.draw(map); }
        }
    }
}
