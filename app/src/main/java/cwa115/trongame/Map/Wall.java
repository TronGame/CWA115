package cwa115.trongame.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

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
     * @param point The point that the distance is measured to
     * @return the distance between the point and the wall.
     */
    public double getDistanceTo(LatLng point) {
        return LatLngConversion.getDistanceToLine(point, points);
    }

    public boolean hasCrossed(LatLng last, LatLng current, double minDistance, boolean removeLastAdded) {
        boolean awayFromPlayer = !removeLastAdded;
        ArrayList<LatLng> points = new ArrayList<>(this.points);
        int n = points.size()-1;
        while (n>=0 && !awayFromPlayer) {
            if (LatLngConversion.getDistancePoints(current, points.get(n)) < minDistance)
                points.remove(n);
            else
                awayFromPlayer = true;
            n+=1;
        }
        double distance = LatLngConversion.getDistanceToLine(current, points);
        if (distance < minDistance) {return true; }

        // Has the player crossed the wall ?
        // TODO: this may cause a problem if the player cycles around the end of a wall !
        boolean hasCrossed = false;
        Vector2D pt1 = new Vector2D(current);
        Vector2D pt2 = new Vector2D(last);
        int end = points.size();

        for (int i = 0; i < end; i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            if (i + 1 < end) {
                Vector2D ptB = new Vector2D(points.get(i + 1));

                // Direction vector of the line AB
                Vector2D ab = ptB.subtract(ptA);
                ab = ab.divide(ab.getLength());

                // Direction vector perpendicular to the line AB
                Vector2D v = new Vector2D(ab.y, -ab.x);

                Vector2D r1 = ptA.subtract(pt1);  // Vector from pt1 to ptA
                Vector2D f1 = ptB.subtract(pt1);  // Vector from pt1 to ptB
                
                double a1 = ab.product(r1);       // Length of the projection of r1 on the line AB
                double b1 = ab.product(f1);       // Length of the projection of f1 on the line AB
                double v1 = v.product(r1);        // Length of the projection of r1 on v
                
                Vector2D r2 = ptA.subtract(pt2);  // Vector from pt2 to ptA
                Vector2D f2 = ptB.subtract(pt2);  // Vector from pt2 to ptB

                double a2 = ab.product(r2);       // Length of the projection of r2 on the line AB
                double b2 = ab.product(f2);       // Length of the projection of f2 on the line AB
                double v2 = v.product(r1);        // Length of the projection of r2 on v

                // if a and b have the same sign the projections of r and f on AB
                // are facing the same direction. This means that the perpendicular projection
                // of pt on AB isn't in between ptA and ptB. We check this for both current and last
                // current and last also need to be on opposite sides of the line segment
                // this is what the last check verifies
                if (Math.signum(a1 * b1) != -1 && Math.signum(a2 * b2) != -1 && Math.signum(v1 * v2) != -1) {
                    hasCrossed = true;
                    break;
                }
            }
        }
            
        return hasCrossed;
    }

    /**
     * Get the id of the owner of the wall
     */
    public String getOwnerId() {
        return ownerId;
    }
}
