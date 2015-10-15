package cwa115.trongame.Map;


import com.google.android.gms.maps.GoogleMap;

/**
 * Stores information (e.g. item locations) of the map.
 */
public class Map {
    // Store the GoogleMap object to draw on it later
    private GoogleMap map;

    // Store the players currently in the game
    private Player[] players;

    /**
     * Class initializer
     */
    public Map(String[] player_names) {

        // Create the array of players
        players = new Player[player_names.length];
        for (int i=0; i<player_names.length; i++) {
            players[i] = new Player(player_names[i]);
        }

    }


    /**
     * Set the GoogleMap object
     */
    public void setMap(GoogleMap _map) {
        map = _map;
    }
}
