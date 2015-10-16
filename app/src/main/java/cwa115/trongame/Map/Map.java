package cwa115.trongame.Map;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Stores information (e.g. item locations) of the map.
 */
public class Map {
    // Store the items currently in the game
    private HashMap<String, DrawableMapItem> mapItems;

    /**
     * Class initializer
     * @param players array of players to be added to the map.
     */
    public Map(Player[] players) {
        mapItems = new HashMap<>();
        // Store the players
        for(Player player : players) {
            mapItems.put(player.get_id(), player);
        }
    }

    /**
     * Update a player map item
     * @param id the player identifier (i.e. username)
     * @param location player location, expressed as a (latitude, longitude) pair
     */
    public void updatePlayer(String id, LatLng location) {
        Player player = (Player)mapItems.get(id);
        player.setLocation(location);
    }

    public void draw(GoogleMap map) {
        for(DrawableMapItem item : mapItems.values()) {
            item.draw(map);
        }
    }
}
