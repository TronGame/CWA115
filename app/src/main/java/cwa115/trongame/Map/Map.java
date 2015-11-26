package cwa115.trongame.Map;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
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
    private int MapZoom = 15;                               // The default zoom on the map

    private MapFragment mapFragment;                        // MapFragment used to draw on

    private HashMap<String, DrawableMapItem> mapItems;      // Stores the items currently on the map
    private ArrayList<Wall> walls;                          // Stores the walls currently on the map
    private ArrayList<DrawableMapItem> pendingItemsDraw;    // The items that still need to be redrawn
    private ArrayList<DrawableMapItem> pendingItemsClear;   // The items that still need to be cleared

    private CameraUpdate cameraUpdate;                      // The next camera position update

    /**
     * Class initializer
     * @param _mapFragment the map fragment object used to draw on
     */
    public Map(MapFragment _mapFragment) {
        mapFragment = _mapFragment;                 // The map fragment, used to draw on
        mapItems = new HashMap<>();                 // The list of items that are drawn on the map (contains walls as well)
        walls = new ArrayList<>();                  // The list of walls that are drawn on the map
        pendingItemsDraw = new ArrayList<>();       // The list of items that still have to be updated
        pendingItemsClear = new ArrayList<>();      // The list of items that need to be cleared

        // cameraUpdate = CameraUpdateFactory.zoomTo(MapZoom); // The next update for the camera
        mapFragment.getMapAsync(this);                      // Update the map (on a different thread)
    }

    /**
     * Get all of the wall objects on the map
     */
    public ArrayList<Wall> getWalls() {
        return walls;
    }

    /**
     * Add a map item to draw on the map
     * @param item the item that needs to be added
     */
    public void addMapItem(DrawableMapItem item) {
        if (mapItems.keySet().contains(item.getId())) {
            return;
        }
        // Is the item a wall?
        if (item instanceof Wall)
            walls.add((Wall)item);

        // Draw the item on the wall
        mapItems.put(item.getId(), item);
        redraw(item.getId());
    }

    /**
     * Remove an item from the map
     * @param itemId The item that needs to be removed
     */
    public void removeMapItem(String itemId) {
        if (!mapItems.keySet().contains(itemId)) {
            return;
        }
        DrawableMapItem oldItem = mapItems.get(itemId);
        if (oldItem instanceof Wall) {
            walls.remove(oldItem);
        }
        clear(itemId);
        mapItems.remove(itemId);
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
            if(item != null)
                pendingItemsDraw.add(item);
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Redraw a single item
     * @param itemId item id to redraw
     */
    public void redraw(String itemId) {
        DrawableMapItem item = mapItems.get(itemId);
        if(item != null)
            pendingItemsDraw.add(item);

        try {
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            return;
        }

        return;
    }

    /**
     * Redraw a set of items
     * @param itemIds item ids to redraw
     */
    public void clear(String[] itemIds) {
        for (String itemId : itemIds) {
            DrawableMapItem item = mapItems.get(itemId);
            if(item != null)
                pendingItemsClear.add(item);
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Clear a single item
     * @param itemId item id to redraw
     */
    public void clear(String itemId) {
        DrawableMapItem item = mapItems.get(itemId);
        if(item != null)
            pendingItemsClear.add(item);

        mapFragment.getMapAsync(this);
    }

    public void updateCamera(LatLng position) {
        cameraUpdate = CameraUpdateFactory.newLatLng(position);
        mapFragment.getMapAsync(this);
    }

    /**
     * Activates when the GoogleMap object is ready for use (getMapAsync finishes)
     * @param map note: cannot be stored since it will be destroyed when this callback exits
     */
    @Override
    public void onMapReady(GoogleMap map) {
        if (cameraUpdate != null) {
            map.animateCamera(cameraUpdate);
            cameraUpdate = null;
        }

        for(DrawableMapItem item : pendingItemsDraw)
            item.draw(map);

        for(DrawableMapItem item : pendingItemsClear)
            item.clear(map);

        pendingItemsClear.clear();
        pendingItemsDraw.clear();
    }

    /**
     * @return true if the given id corresponds to an existing DrawableMapItem
     */
    public boolean hasObject(String id)
    {
        return mapItems.containsKey(id);
    }

    public DrawableMapItem getItemById(String id)
    {
        return mapItems.get(id);
    }
}
