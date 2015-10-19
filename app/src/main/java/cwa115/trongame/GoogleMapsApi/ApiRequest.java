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

    public ApiRequest(boolean interpolate, String key, LatLng[] points) {
        this.interpolate = interpolate;
        this.key = key;
        this.points = points;
    }

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

    public String getPath() {
        return TextUtils.join("|", points);
    }
}