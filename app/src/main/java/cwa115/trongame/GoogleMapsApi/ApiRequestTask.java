package cwa115.trongame.GoogleMapsApi;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;

import cwa115.trongame.R;

/**
 * Sends a Google Maps API request using HTTPS.
 */
public class ApiRequestTask implements Runnable {

    private Handler callback;
    private ApiRequest request;
    private String userAgent;

    /**
     * Constructor
     * @param handlerClass handler to call when the request finishes
     * @param apiRequest the request to run
     */
    public ApiRequestTask(Handler handlerClass, ApiRequest apiRequest) {
        this(handlerClass, apiRequest, null);
    }

    /**
     * Constructor
     * @param handlerClass handler to call when the request finishes
     * @param apiRequest the request to run
     * @param userAgent the user agent to use for requests, defaults to @string/user_agent if unset
     */
    public ApiRequestTask(Handler handlerClass, ApiRequest apiRequest, String userAgent)
    {
        callback = handlerClass;
        request = apiRequest;
        if(userAgent == null)
            this.userAgent = Resources.getSystem().getString(R.string.user_agent);
        else
            this.userAgent = userAgent;
    }

    /**
     * Run the request on the calling thread.
     */
    public void run() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)request.getUrl().openConnection();
            connection.setRequestMethod("GET");
            // TODO: Make user agent configurable
            connection.setRequestProperty(
                    "User-Agent", userAgent
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

    /**
     * Parse the JSON response and invoke the callback
     * @param reader the JsonReader constructed from the input stream of the request result
     */
    public void sendResponse(JsonReader reader) {
        ApiResponse response = new ApiResponse(reader);
        Message m = new Message();
        m.setData(response.getBundle());
        callback.sendMessage(m);
    }

}
