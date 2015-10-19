package cwa115.trongame.Map;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;

import cwa115.trongame.GoogleMapsApi.ApiRequest;
import cwa115.trongame.GoogleMapsApi.ApiRequestTask;
import cwa115.trongame.GoogleMapsApi.ApiResponse;
import cwa115.trongame.R;

/**
 * A set of connected points representing a wall.
 */
public class Wall implements DrawableMapItem, Handler.Callback {

    private String id;
    private ArrayList<LatLng> points;
    private int lineWidth = 5;
    private Polyline line;
    private Thread pointSnapThread;

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

        ApiRequest request = new ApiRequest(
                true,
                Resources.getSystem().getString(R.string.google_maps_key_server),
                points.toArray(new LatLng[points.size()]));

        ApiRequestTask task = new ApiRequestTask(new Handler(this), request);
        pointSnapThread = new Thread(task);
        pointSnapThread.start();
    }

    /**
     * Draws the Wall on the map.
     * @param map the GoogleMap to draw on
     */
    @Override
    public void draw(GoogleMap map) {
        if (line != null)
            line.remove();

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

    public boolean handleMessage(Message message) {
        ApiResponse response = new ApiResponse(message.getData());
        points = response.getPoints();
        return true;
    }
}
