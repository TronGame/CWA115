package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Lists.CustomAdapter;
import cwa115.trongame.Lists.ListItem;
import cwa115.trongame.Network.HttpConnector;

public class LobbyActivity extends AppCompatActivity {

    final static int GAME_LIST_REFRESH_TIME = 1000;

    private String dataToPut;
    private ListView lobbyList;
    private HttpConnector dataServer;
    private HashMap<String,Integer> roomIds;
    private Timer gameListUpdater;
    private Handler gameListHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        gameListUpdater = new Timer();
        gameListHandler = new Handler() {
            public void handleMessage(Message msg) {
                listGames();
            }
        };
        gameListUpdater.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                gameListHandler.sendMessage(new Message());
            }
        }, 0, GAME_LIST_REFRESH_TIME);
        listGames();
    }
    public void showHostingActivity(View view) {
        startActivity(new Intent(this, HostingActivity.class));
    }

    private void listGames() {
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

    public void createLobby(JSONArray result) {

        List<ListItem> listOfRooms = new ArrayList<>();
        roomIds = new HashMap();

        try {
            for(int i = 0; i < result.length(); i++) {
                JSONObject newRoom = result.getJSONObject(i);
                listOfRooms.add(new ListItem(
                        newRoom.getString("name"),
                        newRoom.getString("owner"),
                        newRoom.getString("maxPlayers")
                ));
                roomIds.put(newRoom.getString("name"),newRoom.getInt("id"));
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
