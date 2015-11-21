package cwa115.trongame.Map;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.GoogleMapsApi.ApiListener;
import cwa115.trongame.GoogleMapsApi.SnappedPointHandler;
import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Line;
import cwa115.trongame.Utils.PolyLineUtils;
import cwa115.trongame.Utils.Vector2D;

/**
 * A set of connected points representing a wall.
 */
public class Wall implements DrawableMapItem, ApiListener<ArrayList<LatLng>> {

    static final private int LINE_WIDTH = 15;
    static final private double SPLIT_DISTANCE = LatLngConversion.meterToLatLngDistance(20);
    static final private double HOLE_SIZE = LatLngConversion.meterToLatLngDistance(20);

    private String id;                      // The wall id
    private String ownerId;                 // The id of the owner
    private ArrayList<LatLng> points;       // The list of points that the wall is made out of
    private Polyline line;                  // The line object that is drawn on the map
    private int color;                      // The color of the wall

    private SnappedPointHandler snappedPointHandler;    // Controls location snapping
    private GeoApiContext context;                      // The context that takes care of the location snapping

    public Wall(String id, String ownerId, int color, GeoApiContext context) {
        this.id = id;
        this.ownerId = ownerId;
        this.color = color;
        this.points = new ArrayList<>();
        this.context = context;
    }

    /**
     * Construct from list of points
     */
    public Wall(String id, String ownerId, int color, ArrayList<LatLng> points, GeoApiContext context) {
        this.id = id;
        this.ownerId = ownerId;
        this.color = color;
        this.points = points;
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
        return PolyLineUtils.getDistanceToLine(new Vector2D(point), points);
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
        double distance = PolyLineUtils.getDistanceToLine(new Vector2D(current), points);
        if (distance < minDistance) {return true; }

        // Has the player crossed the wall ?
        // TODO: this may cause a problem if the player cycles around the end of a wall
        Vector2D pt1 = new Vector2D(current);
        Vector2D pt2 = new Vector2D(last);
        int end = points.size()-1;

        for (int i = 0; i < end; i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            Vector2D ptB = new Vector2D(points.get(i + 1));

            Line line1 = new Line(ptA, ptB);
            Line line2 = new Line(pt1, pt2);

            Vector2D intersect = line1.getIntersect(line2);

            if (line1.isOn(intersect)) {
                    return true;
            }
        }
            
        return false;
    }

    /**
     * Get the id of the owner of the wall
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Split a wall in two
     * @param point the point at which the wall will be split
     */
    public ArrayList<Wall> splitWall(LatLng point) {
        if (getDistanceTo(point) > HOLE_SIZE)
            return null;

        ArrayList<Wall> newWalls = new ArrayList<>();
        ArrayList<ArrayList<LatLng>> newWallPoints = createHole(HOLE_SIZE, point);
        for (int i=0; i<newWallPoints.size(); i++) {
            newWalls.add(
                    new Wall(id+"_"+i, ownerId, color, newWallPoints.get(i), context)
            );
        }
        return newWalls;
    }

    /**
     * Create a hole in the points list
     * @param dist The size of the hole
     * @param point The point of the player at the moment the hole is created
     * @return The points of the new walls
     */
    private ArrayList<ArrayList<LatLng>> createHole(double dist, LatLng point) {
        if (points.size() <= 0)
            return null;

        ArrayList<ArrayList<LatLng>> result = new ArrayList<>();

        boolean isFar = LatLngConversion.getDistancePoints(point, points.get(0)) > dist;
        ArrayList<LatLng> currentWallPart = new ArrayList<>();
        Vector2D pt = new Vector2D(point);

        for (int i=0; i<points.size()-1; i++) {
            // If the current point is not inside of the hole it can be added to the current wall.
            if (isFar)
                currentWallPart.add(points.get(i));

            // Check if the current segment intersects with the hole boundary
            Line line = new Line(new Vector2D(points.get(i)), new Vector2D(points.get(i+1)));
            Vector2D[] newPoints = line.getPointAtDist(dist, pt);
            for (Vector2D newPt : newPoints) {
                if (line.isOn(newPt)) {
                    if (isFar) {
                        // End the current wall (we have entered the hole)
                        currentWallPart.add(new LatLng(newPt.x, newPt.y));
                        isFar = false;
                        result.add(new ArrayList<>(currentWallPart));
                        currentWallPart = new ArrayList<>();
                    } else {
                        // Start a new wall (we have left the hole)
                        currentWallPart.add(new LatLng(newPt.x, newPt.y));
                        isFar = true;
                    }
                }
            }
        }

        return result;
    }
}
