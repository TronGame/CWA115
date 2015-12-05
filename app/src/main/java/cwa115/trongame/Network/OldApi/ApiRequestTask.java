package cwa115.trongame.Network.OldApi;

import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Sends a Google Maps API request using HTTPS.
 */
public class ApiRequestTask implements Runnable {

    private static final String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 5.0;Windows98;DigExt)";
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
            this.userAgent = DEFAULT_USER_AGENT;
        else
            this.userAgent = userAgent;
    }

    /**
     * Run the request on the calling thread.
     */
    public void run() {
        try {
            URLConnection connection = request.getUrl().openConnection();
            //connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setDoInput(true);
            connection.connect();

            //switch(connection.getResponseCode()) {
            //    case 200:
            //    case 201:
                    JsonReader reader = new JsonReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    sendResponse(reader);
            //    default:
                    sendResponse(null); // TODO: error handling
            //}
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
