package cwa115.trongame.Utils;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Offers various utility functions to convert LatLng objects.
 */
public class LatLngConversion {
    // region Distances
    final static double METER_TO_LATLNG = 0.863256867e-5;

    /**
     * Convert a distance in meter to a distance between LatLngs.
     * @param distance the distance to be converted
     * @return the converted distance
     */
    public static double latLngDistanceToMeter(double distance) {
        return distance / METER_TO_LATLNG;
    }

    /**
     * Convert a distance between LatLngs to a distance in meter.
     * @param distance the distance to be converted
     * @return the converted distance
     */
    public static double meterToLatLngDistance(double distance) {
        return distance * METER_TO_LATLNG;
    }

    /**
     * Returns the distance between a and b
     * @param a the first point
     * @param b the second point
     * @return the distance
     */
    public static double getDistancePoints(LatLng a, LatLng b) {
        return new Vector2D(a).subtract(new Vector2D(b)).getLength();
    }

    /**
     * Returns the distance between a point and a wall.
     * This distance is defined as the closest distance to either one of the segments of the wall
     * or one of the points of the wall. The distance to a wall segment is however only defined
     * when perpendicular from the point to the wall segment intersects the segment.
     * @param point The point that the distance is measured to
     * @param points The points that make up the wall
     * @return the distance between the point and the wall.
     */
    public static double getDistanceToLine(LatLng point, ArrayList<LatLng> points) {
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
    // endregion

    // region Snapped Point Conversion
    /**
     * Convert a single LatLng to a com.google.maps.model.LatLng
     * @param point the point to convert
     * @return the converted point
     */
    public static com.google.maps.model.LatLng getConvertedPoint(LatLng point) {
        return new com.google.maps.model.LatLng(
                point.latitude,
                point.longitude
        );
    }

    /**
     * Convert points to com.google.maps.model.model.LatLng[].
     * @return the converted points
     */
    public static com.google.maps.model.LatLng[] getConvertedPoints(ArrayList<LatLng> points) {
        com.google.maps.model.LatLng[] convertedPoints =
                new com.google.maps.model.LatLng[points.size()];

        for(int i = 0; i<points.size(); i++)
            convertedPoints[i] = getConvertedPoint(points.get(i));
        return convertedPoints;
    }


    /**
     * Convert SnappedPoints[] to points.
     * @return the converted points
     */
    public static ArrayList<LatLng> snappedPointsToPoints(SnappedPoint[] snappedPoints) {
        ArrayList<LatLng> result = new ArrayList<>();
        for (int i = 0; i<snappedPoints.length; i++)
            result.add(new LatLng(
                    snappedPoints[i].location.lat,
                    snappedPoints[i].location.lng));

        return result;
    }
    // endregion

    // region JSON Conversion
    /**
     * Extracts a point from a given JSON object.
     * @param object the given JSON object
     * @return the extracted point
     */
    public static LatLng getPointFromJSON(JSONObject object) {
        try {
            return new LatLng(object.getDouble("latitude"), object.getDouble("longitude"));
        } catch(JSONException e) {
            return new LatLng(0, 0);
        }
    }

    /**
     * Creates a JSON object from a point.
     * @param point the given LatLng
     * @return a JSON object representing the given point
     */
    public static JSONObject getJSONFromPoint(LatLng point) {
        JSONObject result = new JSONObject();
        try {
            result.put("latitude", point.latitude);
            result.put("longitude", point.longitude);
        } catch(JSONException e) {
            // TODO: do something useful
        }
        return result;
    }
    // endregion

    // region Bundle Conversion
    /**
     * Convert an array of SnappedPoints to a bundle
     * @param points the array of snapped points
     * @return the bundle containing the snapped points
     */
    public static Bundle toBundle(SnappedPoint[] points) {
        double[] latitudes = new double[points.length];
        double[] longitudes = new double[points.length];

        for (int i=0; i<points.length; i++) {
            latitudes[i] = points[i].location.lat;
            longitudes[i] = points[i].location.lng;
        }

        // Create the bundle from the latitudes and the longitudes
        Bundle bundle = new Bundle();
        bundle.putDoubleArray("lat", latitudes);
        bundle.putDoubleArray("lng", longitudes);

        return bundle;
    }

    /**
     * Convert a bundle to an array of LatLngs
     * @param bundle the bundle containing the points
     * @return the array of LatLngs
     */
    public static ArrayList<LatLng> bundleToLatLng(Bundle bundle) {
        double[] latitudes = bundle.getDoubleArray("lat");
        double[] longitudes = bundle.getDoubleArray("lng");

        if (longitudes == null || latitudes == null) {
            Log.d("ERROR", "this shouldn't have happened?");
            return null;
        }

        ArrayList<LatLng> result =  new ArrayList<>();

        for (int i=0; i<latitudes.length; i++) {
            result.add(new LatLng(latitudes[i], longitudes[i]));
        }

        return result;
    }
    // endregion
}
