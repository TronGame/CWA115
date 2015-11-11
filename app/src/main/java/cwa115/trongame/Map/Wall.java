package cwa115.trongame.Map;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;
import java.util.Arrays;

import cwa115.trongame.GoogleMapsApi.ApiListener;
import cwa115.trongame.GoogleMapsApi.SnappedPointHandler;
import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Vector2D;

/**
 * A set of connected points representing a wall.
 */
public class Wall implements DrawableMapItem, ApiListener<ArrayList<LatLng>> {

    static final private int LINE_WIDTH = 15;

    private String id;                      // The wall id
    private String ownerId;                 // The id of the owner
    private ArrayList<LatLng> points;       // The list of points that the wall is made out of
    private Polyline line;                  // The line object that is drawn on the map
    private int color;                      // The color of the wall

    private SnappedPointHandler snappedPointHandler;    // Controls location snapping
    private GeoApiContext context;                      // The context that takes care of the location snapping

    /**
     * Construct from an array of points.
     */
    public Wall(String id, String ownerId, int color, GeoApiContext context) {
        this.id = id;
        this.ownerId = ownerId;
        this.color = color;
        this.points = new ArrayList<>();
        this.context = context;
    }

    /**
     * Extend the wall with one point.
     * @param point the given point
     */
    public void addPoint(LatLng point) {
        points.add(point);
        if (points.size() == 1)
            points.add(point);

        // Create a PendingResult object
        // This is used by the snappedPointHandler to snap the provided points to the road
        PendingResult<SnappedPoint[]> req = RoadsApi.snapToRoads(
                context,                                    // The context (basically the api key used by google
                true,                                       // Interpolate the points (add new points to smooth the line
                LatLngConversion.getConvertedPoints(points) // The points that need to be snapped (these need to be converted to a different type of LatLng
        );
        // Check if the snappedPointHandler is still busy with a previous request
        if (snappedPointHandler != null && !snappedPointHandler.isFinished())
            snappedPointHandler.stop(); // Cancel the previous request

        // Create a new snappedPointHandler to take care of the request
        snappedPointHandler = new SnappedPointHandler(req, this);
    }

    @Override
    public boolean handleApiResult(ArrayList<LatLng> result) {
        points = result;
        return false;
    }


    /**
     * Draws the Wall on the map.
     * @param map the GoogleMap to draw on
     */
    @Override
    public void draw(GoogleMap map) {
        if (line != null)
            line.remove();

        line = map.addPolyline(
            new PolylineOptions()
                .add(points.toArray(new LatLng[points.size()]))
                .width(LINE_WIDTH)
                .color(color)
        );
    }

    public void clear(GoogleMap map) {
        if (line != null) {
            line.remove();
        }
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the distance between a point and the wall.
     * This distance is defined as the closest distance to either one of the segments of the wall
     * or one of the points of the wall. The distance to a wall segment is however only defined
     * when perpendicular from the point to the wall segment intersects the segment.
     * @param point The point that the distance is measured to
     * @return the distance between the point and the wall.
     */
    public double getDistanceTo(LatLng point) {
        Vector2D pt = new Vector2D(point);

        double shortestPerpendDist = Double.POSITIVE_INFINITY;
        double shortestPointDist = Double.POSITIVE_INFINITY;

        int end = points.size();

        for (int i = 0; i < end; i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            double currentDistance = ptA.subtract(pt).getLength();

            if (currentDistance <= shortestPointDist)
                shortestPointDist = currentDistance;

            if (i + 1 < end) {
                Vector2D ptB = new Vector2D(points.get(i+1));

                // Direction vector of the line AB
                Vector2D ab = ptB.subtract(ptA);
                ab = ab.divide(ab.getLength());

                // Direction vector perpendicular to the line AB
                Vector2D v = new Vector2D(ab.y, -ab.x);

                Vector2D r = ptA.subtract(pt);  // Vector from pt to ptA
                Vector2D f = ptB.subtract(pt);  // Vector from pt to ptB

                double a = ab.product(r);       // Length of the projection of r on the line AB
                double b = ab.product(f);       // Length of the projection of r on the line AB

                // if a and b have the same sign the projections of r and f on AB
                // are facing the same direction. This means that the perpendicular projection
                // of pt on AB isn't in between ptA and ptB. If this was the case the distance
                // to the line AB can't be taken into account.
                if (Math.signum(a*b)!=-1 && shortestPerpendDist > v.product(r)) {
                    shortestPerpendDist = Math.abs(v.product(r));
                }
            }
        }

        if (shortestPerpendDist == Double.POSITIVE_INFINITY)
            return shortestPointDist;
        else
            return Math.min(shortestPointDist, shortestPointDist);
    }

    /**
     * Get the id of the owner of the wall
     */
    public String getOwnerId() {
        return ownerId;
    }
}
