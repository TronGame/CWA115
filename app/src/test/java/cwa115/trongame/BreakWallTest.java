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
public class BreakWallTest {
    LatLng[] points;
    GeoApiContext context;

    @Before
    public void setup() {
        points = new LatLng[]{
                new LatLng(0.0, 0.0),
                new LatLng(0.0, 3.0),
                new LatLng(3.0, 6.0),
                new LatLng(0.0, 9.0),
                new LatLng(0.0, 12.0)
        };
        context = new GeoApiContext().setApiKey(
                "AIzaSyBCT-9-ApbKK22DlqOMywuMLnOe6mblRik");
    }

    @Test
    public void testBreakWall()  {
        Wall wall = new Wall("test", "test", Color.RED, new ArrayList<LatLng>(Arrays.asList(points)), context);
        // Normal test
        LatLng pt1 = new LatLng(5.0, 6.0);
        ArrayList<Wall> newWalls = wall.splitWall(pt1, 3.0);
        assertEquals(newWalls.size(), 3);

        // Test at point of wall
        pt1 = new LatLng(0.0, 0.0);
        newWalls = wall.splitWall(pt1, 3.0);
        assertEquals(newWalls.size(), 2);
    }
}
