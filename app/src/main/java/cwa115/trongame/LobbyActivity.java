package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.Lists.CustomAdapter;
import cwa115.trongame.Lists.ListItem;
import cwa115.trongame.Network.HttpConnector;

public class LobbyActivity extends AppCompatActivity {

    private String dataToPut;
    private ListView lobbyList;
    private HttpConnector dataServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        dataServer.sendRequest("listGames", new HttpConnector.Callback() {

            @Override
            public void handleResult(String data) {
                try {
                    createLobby(new JSONArray(data));
                } catch(JSONException e) {
                    // Ignore failed requests
                    Log.d("DATA SERVER", e.toString());
                }
            }
        });
    }
    public void showHostingActivity(View view) {
        startActivity(new Intent(this, HostingActivity.class));
    }

    public void createLobby(JSONArray result) {

        List<ListItem> listOfRooms = new ArrayList<>();

        try {
            for(int i = 0; i < result.length(); i++) {
                JSONObject newRoom = result.getJSONObject(i);
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
