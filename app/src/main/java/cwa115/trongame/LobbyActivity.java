package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.LobbyCustomAdapter;
import cwa115.trongame.Lists.LobbyListItem;
import cwa115.trongame.Network.HttpConnector;

public class LobbyActivity extends AppCompatActivity {

    final static int GAME_LIST_REFRESH_TIME = 1000;

    private String dataToPut;
    private HttpConnector dataServer;
    private HashMap<String,Integer> roomIds;
    private Timer gameListUpdater;
    private Handler gameListHandler;
    private List<LobbyListItem> listOfRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ListView lobbyList = (ListView) findViewById(R.id.mainList);
        lobbyList.setClickable(true);
        gameListUpdater = new Timer();
        gameListHandler = new Handler() {
            @Override
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

    private void listGames() {
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        dataServer.sendRequest("listGames", new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    createLobby(new JSONArray(data));
                } catch (JSONException e) {
                    // Ignore failed requests
                    Log.d("DATA SERVER", e.toString());
                }
                ListView lobbyList = (ListView) findViewById(R.id.mainList);
                lobbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
                        LobbyListItem clickedItem = (LobbyListItem) listOfRooms.get(position);
                        String gameName = clickedItem.getGamename();

                        GameSettings.setGameName(gameName);
                        GameSettings.setGameId(roomIds.get(gameName));
                        showToast("Joining " + gameName);

                        final String query = "joinGame?"+
                                "gameId="+roomIds.get(gameName) +
                                "&id="+GameSettings.getPlayerId()+
                                "&token="+GameSettings.getPlayerToken();

                        dataServer.sendRequest(query, new HttpConnector.Callback() {
                            @Override
                            public void handleResult(String data) {
                                try {
                                    JSONObject result = new JSONObject(data);
                                    // TODO check for errors
                                    showRoomActivity();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void showHostingActivity(View view) {
        startActivity(new Intent(this, HostingActivity.class));
    }

    private void showRoomActivity() {
        startActivity(new Intent(this, RoomActivity.class));
    }

    public void createLobby(JSONArray result) {

        listOfRooms = new ArrayList<>();
        roomIds = new HashMap();

        try {
            for(int i = 0; i < result.length(); i++) {
                JSONObject newRoom = result.getJSONObject(i);
                listOfRooms.add(new LobbyListItem(
                        newRoom.getString("name"),
                        newRoom.getString("owner"),
                        newRoom.getString("maxPlayers")
                ));
                roomIds.put(newRoom.getString("name"),newRoom.getInt("id"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        ListView lobbyList = (ListView) findViewById(R.id.lobby_list);
        lobbyList = (ListView) findViewById(R.id.mainList);
        lobbyList.setClickable(true);
        LobbyCustomAdapter adapter = new LobbyCustomAdapter(this, listOfRooms);
        lobbyList.setAdapter(adapter);

    }

    private void showToast(String text) {
        Toast.makeText(
                getBaseContext(), text,
                Toast.LENGTH_SHORT
        ).show();
    }
}
