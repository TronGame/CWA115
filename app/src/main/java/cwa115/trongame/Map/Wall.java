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

import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Vector2D;

/**
 * A set of connected points representing a wall.
 */
public class Wall implements DrawableMapItem {

    private String id;
    private ArrayList<LatLng> points;
    private ArrayList<LatLng> notSnappedPoints;
    private int lineWidth = 35;
    private Polyline line;
    private GeoApiContext context;
    private PendingResult<SnappedPoint[]> req;

    /**
     * Construct from an array of points.
     * @param points points of the wall
     */
    public Wall(String _id, LatLng[] points, GeoApiContext context) {
        id = _id;
        this.points = new ArrayList<>();
        this.notSnappedPoints = new ArrayList<>(Arrays.asList(points));
        this.context = context;
    }

    /**
     * Extend the wall with one point.
     * @param point the given point
     */
    public void addPoint(LatLng point) {
        notSnappedPoints.add(point);

        if (req != null) {
            req.cancel();
        }

        req = RoadsApi.snapToRoads(
                    context,
                    true,
                    LatLngConversion.getConvertedPoints(this.notSnappedPoints)
        );

        // WARNING: This can cause huuuuge problems for various reasons
        req.setCallback(new PendingResult.Callback<SnappedPoint[]>() {
            @Override
            public void onResult(SnappedPoint[] result) {
                points = LatLngConversion.snappedPointsToPoints(result);
                req = null;
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
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

        double shortestPerpendDist = -1;
        double shortestPointDist = 0;

        for (int i = 0; i < points.size(); i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            double currentDistance = ptA.subtract(pt).getLength();

            if (currentDistance <= shortestPointDist)
                shortestPointDist = currentDistance;

            if (i + 1 < points.size()) {
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

        if (shortestPerpendDist < 0)
            return shortestPointDist;
        else
            return Math.min(shortestPointDist, shortestPointDist);
    }
}
