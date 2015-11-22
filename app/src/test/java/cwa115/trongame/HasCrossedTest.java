package cwa115.trongame;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import cwa115.trongame.Map.Wall;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Peter on 22/10/2015.
 */
public class HasCrossedTest {
    LatLng[] points;
    GeoApiContext context;

    @Before
    public void setup() {
        points = new LatLng[]{
                new LatLng(-3.0, 0.0),
                new LatLng(0.0, 0.0),
                new LatLng(0.0, 3.0),
                new LatLng(3.0, 3.0),
                new LatLng(9.0, 6.0)
        };
        context = new GeoApiContext().setApiKey(
                "AIzaSyBCT-9-ApbKK22DlqOMywuMLnOe6mblRik");
    }

    @Test
    public void testHasCrossed()  {
        Wall wall = new Wall("test", "test", Color.RED, new ArrayList<LatLng>(Arrays.asList(points)), context);

        // Test crossing vertical segment
        LatLng pt1 = new LatLng(1.0, 1.0);
        LatLng pt2 = new LatLng(2.0, 5.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 0, ""), true);

        // Test crossing horizontal segment (vertical transfer)
        pt1 = new LatLng(1.0, 1.0);
        pt2 = new LatLng(-1.0, 1.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 0, ""), true);

        // Test crossing vertical segment with horizontal transfer
        pt1 = new LatLng(1.0, 1.0);
        pt2 = new LatLng(1.0, 4.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 0, ""), true);

        // Test perpendicular transfer (horizontal)
        pt1 = new LatLng(-1.0, 1.0);
        pt2 = new LatLng(-1.0, 2.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 0, ""), false);

        // Test perpendicular transfer (vertical)
        pt1 = new LatLng(0.0, -1.0);
        pt2 = new LatLng(-1.0, -1.0);
        assertEquals(wall.hasCrossed(pt1, pt2, 0, ""), false);
    }
}
