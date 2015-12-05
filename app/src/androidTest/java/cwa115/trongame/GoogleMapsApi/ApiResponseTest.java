package cwa115.trongame.GoogleMapsApi;

import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cwa115.trongame.Network.OldApi.ApiResponse;

public class ApiResponseTest extends TestCase {
    double[] longitudes = { 0, 1 };
    double[] latitudes = { 0, 1 };
    String data = "{\"snappedPoints\":["
            + "{\"location\":{\"latitude\":0, \"longitude\":0},\"originalIndex\": 0},"
            + "{\"location\":{\"latitude\":1, \"longitude\":1}}]}";

    public void testGetPoints() throws Exception {
        JsonReader reader = new JsonReader(
                new InputStreamReader(new ByteArrayInputStream(data.getBytes()))
        );
        ApiResponse response = new ApiResponse(reader);

        checkResult(response.getPoints());
    }

    public void checkResult(ArrayList<LatLng> result_list) throws Exception {
        double epsilon = 1e-15;
        for(int i = 0; i < result_list.size(); ++i) {
            assertEquals(longitudes[i], result_list.get(i).longitude, epsilon);
            assertEquals(latitudes[i], result_list.get(i).latitude, epsilon);
        }
    }

    public void testHasError() throws Exception {

    }

    public void testGetBundle() throws Exception {
        JsonReader reader = new JsonReader(
                new InputStreamReader(new ByteArrayInputStream(data.getBytes()))
        );
        ApiResponse response = new ApiResponse(reader);
        ApiResponse from_bundle = new ApiResponse(response.getBundle());

        checkResult(from_bundle.getPoints());
    }
}