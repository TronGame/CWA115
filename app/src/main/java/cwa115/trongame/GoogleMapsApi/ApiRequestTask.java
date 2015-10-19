package cwa115.trongame.GoogleMapsApi;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Sends a Google Maps API request using HTTPS.
 */
public class ApiRequestTask extends AsyncTask<ApiRequest, Void, ApiResponse> {

    @Override
    protected ApiResponse doInBackground(ApiRequest... requests) {
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
                    return new ApiResponse(reader);
                default:
                    return null; // TODO: error handling
            }
        } catch(IOException e) {
            return null; // TODO: error handling
        }
    }

    @Override
    protected void onPostExecute(ApiResponse result) {

    }


}
