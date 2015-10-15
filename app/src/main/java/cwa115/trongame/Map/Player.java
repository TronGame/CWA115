package cwa115.trongame.Map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Peter on 15/10/2015.
 * Controls Player data for drawing on the map
 */
public class Player {

    private String name;        // Store the player name
    private LatLng location;    // Store the player location

    /**
     * Constructor
     */
    public Player(String _name) {
        name = _name;
    }

    /**
     *  Update the player location
     */
    public void setLocation(LatLng _location) {
        location = _location;
    }

}