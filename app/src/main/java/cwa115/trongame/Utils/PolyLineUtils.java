package cwa115.trongame.Utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Peter on 19/11/2015.
 */
public class PolyLineUtils {
    /**
     * Returns the distance between a point and a wall.
     * This distance is defined as the closest distance to either one of the segments of the wall
     * or one of the points of the wall. The distance to a wall segment is however only defined
     * when perpendicular from the point to the wall segment intersects the segment.
     *
     * @param pt     The point that the distance is measured to
     * @param points The points that make up the wall
     * @return the distance between the point and the wall.
     */
    public static double getDistanceToLine(Vector2D pt, ArrayList<LatLng> points) {
        double shortestPerpendDist = Double.POSITIVE_INFINITY;
        double shortestPointDist = Double.POSITIVE_INFINITY;

        int end = points.size();

        for (int i = 0; i < end; i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            double currentDistance = ptA.subtract(pt).getLength();

            if (currentDistance <= shortestPointDist)
                shortestPointDist = currentDistance;

            if (i + 1 < end) {
                Vector2D ptB = new Vector2D(points.get(i + 1));
                Line line = new Line(ptA, ptB);
                double distance = line.distanceTo(pt);
                if (line.isOn(line.getClosestPoint(pt)) && shortestPerpendDist > distance) {
                    shortestPerpendDist = Math.abs(distance);
                }
            }
        }

        if (shortestPerpendDist == Double.POSITIVE_INFINITY)
            return shortestPointDist;
        else
            return Math.min(shortestPointDist, shortestPointDist);
    }

    public static int getClosestPoint(Vector2D pt, ArrayList<LatLng> points) {
        double shortestPerpendDist = Double.POSITIVE_INFINITY;
        int shortestPerpendIndex = -1;
        double shortestPointDist = Double.POSITIVE_INFINITY;
        int shortestPointIndex = -1;

        int end = points.size();

        for (int i = 0; i < end; i++) {
            Vector2D ptA = new Vector2D(points.get(i));
            double currentDistance = ptA.subtract(pt).getLength();

            if (currentDistance <= shortestPointDist) {
                shortestPointDist = currentDistance;
                shortestPointIndex = i;
            }

            if (i + 1 < end) {
                Vector2D ptB = new Vector2D(points.get(i + 1));
                Line line = new Line(ptA, ptB);
                double distance = line.distanceTo(pt);
                if (line.isOn(line.getClosestPoint(pt)) && shortestPerpendDist > distance) {
                    shortestPerpendDist = Math.abs(distance);
                    shortestPerpendIndex = i;
                }
            }
        }

        if (shortestPerpendDist == Double.POSITIVE_INFINITY) {
            return shortestPerpendIndex;
        } else {
            if (shortestPerpendDist <= shortestPointDist)
                return shortestPerpendIndex;
            else
                return shortestPointIndex;
        }
    }
}
