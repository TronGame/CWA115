package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.Lists.CustomAdapter;
import cwa115.trongame.Lists.ListItem;

public class LobbyActivity extends AppCompatActivity {

    String dataToPut;
    ListView lobbyList;

    public String getDataFromServer(String URL){
        String result="";
        try {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                result += output;
            }
            conn.disconnect();

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    };


    public String putDataToServer(String URL){
        String status="Put the data to server successfully!";
        try {

            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //conn.setRequestProperty("Content-Type", "application/json");

            String input = dataToPut;

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();


            //Read the acknowledgement message after putting data to server
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);



        /*createLobby(getDataFromServer(BLABLABLA));*/
    }
    public void showHostingActivity(View view) {
        startActivity(new Intent(this, HostingActivity.class));
    }

    public void createLobby(JSONArray timsArray) {

        List<ListItem> listOfRooms = new ArrayList<ListItem>();

        try {
            for (int i = 0; i < timsArray.length(); i++) {
                JSONObject newRoom = timsArray.getJSONObject(i);
                listOfRooms.add(new ListItem(newRoom.getString("name"), newRoom.getString("owner"),newRoom.getString("maxPlayers").toString() ));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        lobbyList = (ListView) findViewById(R.id.mainList);
        lobbyList.setClickable(true);
        CustomAdapter adapter = new CustomAdapter(this, listOfRooms);
        lobbyList.setAdapter(adapter);

    }
}
