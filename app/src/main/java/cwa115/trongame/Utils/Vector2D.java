package cwa115.trongame.Utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Peter on 21/10/2015.
 */
public class Vector2D {
    public double x;
    public double y;

    public Vector2D(double _x, double _y) {
        x = _x;
        y = _y;
    }

    public Vector2D(LatLng point) {
        x = point.longitude;
        y = point.latitude;
    }

    public double getLength() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D divide(double a) {
        return new Vector2D(x/a, y/a);
    }

    public double product(Vector2D other) {
        return x*other.x + y*other.y;
    }

    public Vector2D product(double a) {
        return new Vector2D(x*a, y*a);
    }

}
