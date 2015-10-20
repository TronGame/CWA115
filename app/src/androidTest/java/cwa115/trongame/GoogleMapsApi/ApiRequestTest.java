package cwa115.trongame.GoogleMapsApi;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class ApiRequestTest extends TestCase {

    public void testGetUrl() throws Exception {
        LatLng[] points = {
                new LatLng(0, 0),
                new LatLng(-1, 1)
        };
        ApiRequest request = new ApiRequest(true, "KEY", points);
        assertEquals(
                "https://roads.googleapis.com/v1/snapToRoads&interpolate=true&key=KEY&points=0.0,0.0|-1.0,1.0",
                request.getUrl().toString()
        );
    }

    public void testGetPath() throws Exception {

    }
}