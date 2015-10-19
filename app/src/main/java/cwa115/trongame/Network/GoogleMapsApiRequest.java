package cwa115.trongame.Network;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * A SnapToRoads Google API request.
 */
class SnapToRoadsRequest {
    private boolean interpolate = true;
    private String key;
    private LatLng[] points;

    public SnapToRoadsRequest(boolean interpolate, String key, LatLng[] points) {
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

/**
 * A SnapToRoads Google API response.
 */
class SnapToRoadsResponse {

    public SnapToRoadsResponse() {


    }
}

/**
 * Sends a Google Maps API request using HTTPS.
 */
public class GoogleMapsApiRequest extends AsyncTask<SnapToRoadsRequest, Void, SnapToRoadsResponse> {

    private SnapToRoadsResponse jsonToResponse(JsonReader reader)
    {
        try {
            SnapToRoadsResponse result = new SnapToRoadsResponse();
            reader.beginObject();

            reader.endObject();
            return result;
        } catch(IOException e) {
            // TODO: error handling
            return null;
        }

    }

    @Override
    protected SnapToRoadsResponse doInBackground(SnapToRoadsRequest... requests) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)requests[0].getUrl().openConnection();
            connection.setRequestMethod("GET");
            // TODO: Make user agent configurable
            connection.setRequestProperty(
                    "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0;Windows98;DigExt)"
            );
            connection.setDoInput(true);
            connection.connect();

            switch(connection.getResponseCode()) {
                case 200:
                case 201:
                    JsonReader reader = new JsonReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    return jsonToResponse(reader);
                default:
                    return null; // TODO: error handling
            }
        } catch(IOException e) {
            return null; // TODO: error handling
        }
    }

    @Override
    protected void onPostExecute(SnapToRoadsResponse result) {

    }


}
