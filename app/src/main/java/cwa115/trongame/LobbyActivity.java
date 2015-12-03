package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.LobbyCustomAdapter;
import cwa115.trongame.Lists.LobbyListItem;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;

public class LobbyActivity extends AppCompatActivity {

    final static int GAME_LIST_REFRESH_TIME = 1000;

    private HttpConnector dataServer;
    private HashMap<String,Integer> roomIds;
    private Timer gameListUpdater;
    private Handler gameListHandler;
    private List<LobbyListItem> listOfRooms;
    private CheckBox checkBoxView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ListView lobbyList = (ListView) findViewById(R.id.mainList);
        checkBoxView = (CheckBox)findViewById(R.id.spectatorCheckboxView);
        lobbyList.setClickable(true);
        listGames();
    }


    @Override
    protected void onResume(){
        super.onResume();
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
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameListUpdater.cancel();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GameSettings.setPlayerName(null);
            GameSettings.setPlayerToken(null);
            GameSettings.setUserId(0);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void listGames() {
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        dataServer.sendRequest(ServerCommand.LIST_GAMES, null, new HttpConnector.Callback() {
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
                        GameSettings.setOwnerId(clickedItem.getHostId());
                        GameSettings.setCanBreakWall(clickedItem.getCanBreakWall());
                        GameSettings.setTimelimit(clickedItem.getTimeLimit());
                        GameSettings.setMaxPlayers(clickedItem.getPlayersAsInteger());
                        GameSettings.setSpectate(checkBoxView.isChecked());
                        showToast("Joining " + gameName);

                        if (!GameSettings.getSpectate()) {
                            Map<String, String> query = ImmutableMap.of(
                                    "gameId", String.valueOf(roomIds.get(gameName)),
                                    "id", String.valueOf(GameSettings.getPlayerId()),
                                    "token", GameSettings.getPlayerToken());

                            dataServer.sendRequest(ServerCommand.JOIN_GAME, query, new HttpConnector.Callback() {
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
                        } else {
                            showRoomActivity();
                        }
                    }
                });
            }
        });
    }

    public void showHostingActivity(View view) {
        gameListUpdater.cancel();
        startActivity(new Intent(this, HostingActivity.class));
    }

    private void showRoomActivity() {
        gameListUpdater.cancel();
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
                        newRoom.getString("ownerName"),
                        newRoom.getInt("owner"),
                        newRoom.getInt("maxPlayers"),
                        (newRoom.getInt("canBreakWall") == 1),
                        newRoom.getInt("timeLimit")
                ));
                roomIds.put(newRoom.getString("name"),newRoom.getInt("id"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        ListView lobbyList = (ListView) findViewById(R.id.mainList);

        // Store the scroll position
        View view = lobbyList.getChildAt(0);
        int indexPosition = lobbyList.getFirstVisiblePosition();
        int top = (view == null) ? 0 : (view.getTop() - lobbyList.getPaddingTop());

        lobbyList.setClickable(true);
        LobbyCustomAdapter adapter = new LobbyCustomAdapter(this, listOfRooms);
        lobbyList.setAdapter(adapter);

        // Reset the scroll position
        lobbyList.setSelectionFromTop(indexPosition, top);

    }

    private void showToast(String text) {
        Toast.makeText(
                getBaseContext(), text,
                Toast.LENGTH_SHORT
        ).show();
    }



    public void showScoreBoard(View view) {
        startActivity(new Intent(this, ScoreBoardActivity.class));
    }
}
