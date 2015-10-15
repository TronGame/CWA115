package cwa115.trongame.Map;


import com.google.android.gms.maps.GoogleMap;

/**
 * Stores information (e.g. item locations) of the map.
 */
public class Map {
    // Store the GoogleMap object to draw on it later
    private GoogleMap map;

    /**
     * Class initializer*
     */
    public Map() {

    }


    /**
     * Set the GoogleMap object
     */
    public void setMap(GoogleMap _map) {
        map = _map;
    }
}
