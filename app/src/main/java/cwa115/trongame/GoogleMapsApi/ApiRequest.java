package cwa115.trongame.GoogleMapsApi;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A SnapToRoads Google API request.
 * TODO: Support multiple request types
 */
class ApiRequest {
    private boolean interpolate = true;
    private String key;
    private LatLng[] points;

    /**
     * Constructor
     * @param interpolate should the API match the curvature of the road
     * @param key the API key
     * @param points the set of points given as an argument to the API
     */
    public ApiRequest(boolean interpolate, String key, LatLng[] points) {
        this.interpolate = interpolate;
        this.key = key;
        this.points = points;
    }

    /**
     * @return the HTTPS URL for the API request
     */
    public URL getUrl() {
        String urlString = "https://roads.googleapis.com/v1/snapToRoads";
        urlString += "&interpolate=" + (interpolate ? "true" : "false");
        urlString += "&key=" + key;
        try {
            return new URL(urlString);
        } catch(MalformedURLException e) {
            // Not going to happen (TODO)
            return null;
        }
    }

    /**
     * @return the set of points as a list
     */
    public String getPath() {
        return TextUtils.join("|", points);
    }
}