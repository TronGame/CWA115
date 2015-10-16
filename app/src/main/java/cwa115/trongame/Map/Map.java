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
     */
    public Map(Player[] players) {
        mapItems = new HashMap<String, DrawableMapItem>();
        // Store the players
        for (int i=0; i<players.length; i++) {
            mapItems.put(players[i].get_id(),
                    players[i]
            );
        }
    }

    /**
     * Update a player map item
     * @param id
     * @param location
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
