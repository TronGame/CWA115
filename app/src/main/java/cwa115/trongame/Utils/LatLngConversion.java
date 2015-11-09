package cwa115.trongame.Utils;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Offers various utility functions to convert LatLng objects.
 */
public class LatLngConversion {

    final static double METER_TO_LATLNG = 0.863256867e-5;

    /**
     * Creates a Bundle from a LatLng object.
     * @param point the point to be converted to a Bundle object
     * @return the newly created Bundle
     */
    public static Bundle getBundleFromPoint(LatLng point) {
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", point.latitude);
        bundle.putDouble("lng", point.longitude);
        return bundle;
    }

    /**
     * Extracts a LatLng object from a given Bundle.
     * @param bundle the Bundle containing the point
     * @return the extracted Point
     */
    public static LatLng getPointFromBundle(Bundle bundle) {
        return new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
    }


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
}
