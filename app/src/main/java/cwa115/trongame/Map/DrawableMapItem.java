package cwa115.trongame.Map;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Peter on 15/10/2015.
 * Used by the map to draw items
 */
public interface DrawableMapItem {

    // The object has to have an id so that it can be found in the hashmap
    public String get_id();

    // Draw the item on the map
    public void draw(GoogleMap map);

}