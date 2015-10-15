package cwa115.trongame.Map;


import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;

/**
 * Stores information (e.g. item locations) of the map.
 */
public class Map {
    // Store the GoogleMap object to draw on it later
    private GoogleMap map;

    // Store the items currently in the game
    private HashMap<String, DrawableMapItem> mapItems;

    /**
     * Class initializer
     */
    public Map(Player[] players) {
        // Store the players
        for (int i=0; i<players.length; i++) {
            mapItems.put(players[i].get_id(),
                    players[i]
            );
        }
    }

    /**
     * Set the GoogleMap object
     */
    public void setMap(GoogleMap _map) {
        map = _map;
    }
}
