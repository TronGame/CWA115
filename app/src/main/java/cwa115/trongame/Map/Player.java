package cwa115.trongame.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Peter on 15/10/2015.
 * Controls Player data for drawing on the map
 */
public class Player implements DrawableMapItem {

    private String id;          // The map item id
    private String name;        // Store the player name
    private LatLng location;    // Store the player location
    private Marker marker;      // The map marker

    /**
     * Constructor
     */
    public Player(String _id, String _name, LatLng _location) {
        id = _id;
        name = _name;
        location = _location;
    }

    /**
     *  Update the player location
     */
    public void setLocation(LatLng _location) {
        location = _location;
    }

    public void draw(GoogleMap map) {
        if(map == null)
            return;
        if(marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(location)
                    .title(name));
        } else {
            marker.setPosition(location);
        }
    }

    public void clear(GoogleMap map) {
        marker.remove();
    }

    public String getId() {
        return id;
    }
}