package cwa115.trongame.Map;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;

import junit.framework.TestCase;

/**
 * Created by Peter on 22/10/2015.
 */
public class WallTest extends TestCase{
    LatLng[] points = {
            new LatLng(0.0, 3.0),
            new LatLng(3.0, 3.0),
            new LatLng(9.0, 6.0)
    };
    GeoApiContext context = new GeoApiContext().setApiKey(
            "AIzaSyBCT-9-ApbKK22DlqOMywuMLnOe6mblRik");
    public void testHasCrossed() throws Exception {
        Wall wall = new Wall("test", "test", Color.RED, context);
        for (LatLng pt : points) {
            wall.addPoint(pt);
        }
        LatLng pt1 = new LatLng(1.0, 1.0);
        LatLng pt2 = new LatLng(2.0, 5.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 100000, false), true);
    }
}
