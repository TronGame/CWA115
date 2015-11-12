package cwa115.trongame.Network;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Allows asynchronous HTTP requests which return application/json data.
 */
public class HttpConnector {

    public interface Callback {
        void handleResult(String data);
    }

    private String serverUrl;
    private static final String RESULT_DATA_KEY = "result";

    private class HTTPRequestTask extends AsyncTask<URL, Void, String> {
        private Handler resultHandler;

        HTTPRequestTask(Handler resultHandler) {
            this.resultHandler = resultHandler;
        }

        public String sendHTTPRequest(URL url){
            String result = "";
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String output;
                while((output = br.readLine()) != null)
                    result += output;
                conn.disconnect();

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected String doInBackground(URL... urls) {
            return sendHTTPRequest(urls[0]);
        }


        @Override
        protected void onPostExecute(String result) {
            Bundle data = new Bundle();
            data.putString(RESULT_DATA_KEY, result);
            Message msg = new Message();
            msg.setData(data);
            resultHandler.sendMessage(msg);

        }
    }

    public HttpConnector(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void sendRequest(String parameters, final Callback callback) {
        try {
            URL requestUrl = new URL(serverUrl + parameters);
            new HTTPRequestTask(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    callback.handleResult(
                            msg.getData().getString(RESULT_DATA_KEY)
                    );
                }
            }).execute(requestUrl);
        } catch(MalformedURLException e) {

        }
    }

}
