package cwa115.trongame.GoogleMapsApi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
public class ApiRequestTask implements Runnable {

    private Handler callback;
    private ApiRequest request;

    ApiRequestTask(Handler handlerClass, ApiRequest apiRequest) {
        callback = handlerClass;
        request = apiRequest;
    }

    public void run() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)request.getUrl().openConnection();
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
                    sendResponse(reader);
                default:
                    sendResponse(null); // TODO: error handling
            }
        } catch(IOException e) {
            sendResponse(null); // TODO: error handling
        }
    }

    public void sendResponse(JsonReader reader) {
        ApiResponse response = new ApiResponse(reader);
        Message m = new Message();
        m.setData(response.getBundle());
        callback.sendMessage(m);
    }

}
