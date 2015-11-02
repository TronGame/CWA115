package cwa115.trongame.Utils;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

/**
 * Offers various utility functions to convert LatLng objects.
 */
public class LatLngConversion {

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
}
