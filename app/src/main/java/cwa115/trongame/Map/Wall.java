package cwa115.trongame.Map;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A set of connected points representing a wall.
 */
public class Wall implements DrawableMapItem {

    private String id;
    private ArrayList<LatLng> points;
    private int lineWidth = 5;
    private Polyline line;

    /**
     * Construct from an array of points.
     * @param given_points points of the wall
     */
    public Wall(String _id, LatLng[] given_points) {
        id = _id;
        points = new ArrayList<>(Arrays.asList(given_points));
    }

    /**
     * Extend the wall with one point.
     * @param point the given point
     */
    public void addPoint(LatLng point) {
        points.add(point);
    }

    /**
     * Draws the Wall on the map.
     * @param map the GoogleMap to draw on
     */
    @Override
    public void draw(GoogleMap map) {
        for(int i = 0; i < points.size() - 1; ++i) {
            line = map.addPolyline(
                    new PolylineOptions()
                    .add(points.get(i), points.get(i + 1))
                    .width(lineWidth)
                    .color(Color.BLUE)
            );
        }

    }

    public void clear(GoogleMap map) {
        line.remove();
    }

    public String getId() {
        return id;
    }
}
