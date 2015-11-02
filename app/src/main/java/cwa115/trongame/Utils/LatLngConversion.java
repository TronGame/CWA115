package cwa115.trongame.Utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

/**
 * Created by Peter on 02/11/2015.
 */
public class LatLngConversion {
    /**
     * Convert points to com.google.maps.model.model.LatLng[].
     * @return the converted points
     */
    public static com.google.maps.model.LatLng[] getConvertedPoints(ArrayList<LatLng> points) {
        com.google.maps.model.LatLng[] convertedPoints =
                new com.google.maps.model.LatLng[points.size()];

        for(int i = 0; i<points.size(); i++)
            convertedPoints[i] = new com.google.maps.model.LatLng(
                    points.get(i).latitude,
                    points.get(i).longitude
            );
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
