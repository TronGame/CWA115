package cwa115.trongame.Utils;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import org.json.JSONArray;
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
     * @return the distance in latlng distance
     */
    public static double getDistancePoints(LatLng a, LatLng b) {
        return new Vector2D(a).subtract(new Vector2D(b)).getLength();
    }

    /**
     * Returns the distance between a and b
     * The calculation here happens more accurately making it better for calculating long distances
     * @param a the first point
     * @param b the second point
     * @return the distance in meters
     */
    public static double getAccurateDistancePoints(LatLng a, LatLng b) {
        float[] results = new float[1];
        Location.distanceBetween(
                a.latitude,
                a.longitude,
                b.latitude,
                b.longitude,
                results);
        return (double)results[0];
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
    /**
     * Extracts points from a given JSON object.
     * @param object the given JSON object
     * @return the extracted point
     */
    public static ArrayList<LatLng> getPointsFromJSON(JSONObject object) {
        try {
            ArrayList<LatLng> points = new ArrayList<>();
            JSONArray list = object.getJSONArray("points");
            for (int i=0; i<list.length(); i++) {
                points.add(getPointFromJSON((JSONObject)list.get(i)));
            }
            return points;
        } catch(JSONException e) {
            // TODO: do something useful
            return null;
        }
    }

    /**
     * Creates a JSON object from an array of points
     * @param points the given LatLng
     * @return a JSON object representing the given point
     */
    public static JSONObject getJSONFromPoints(ArrayList<LatLng> points) {
        try {
            JSONObject result = new JSONObject();
            JSONArray list = new JSONArray();
            for (LatLng point : points) {
                list.put(getJSONFromPoint(point));
            }
            result.put("points", list);
            return result;
        } catch(JSONException e) {
            // TODO: do something useful
            return null;
        }
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
