package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.User.Profile;
import cwa115.trongame.Utils.DrawableManager;

public class LobbyActivity extends AppCompatActivity {

    public final static String JOIN_GAME_EXTRA = "lobbyActivity_joinGameExtra";

    private final static int GAME_LIST_REFRESH_TIME = 1000;

    private HttpConnector dataServer;
    private HashMap<String,Integer> roomIds;
    private Timer gameListUpdater;
    private Handler gameListHandler;
    private List<LobbyListItem> listOfRooms;
    private CheckBox checkBoxView;
    private int gameToJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameToJoin = getIntent().getIntExtra(JOIN_GAME_EXTRA, -1);
        if(gameToJoin!=-1){
            // We started from a notification => make sure basic initializations, normally
            // made in MainActivity are performed here:
            GameSettings.setProfile(Profile.Load(PreferenceManager.getDefaultSharedPreferences(this)));
            DrawableManager.InitializeCache(5);
        }

        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        ListView lobbyList = (ListView) findViewById(R.id.mainList);
        checkBoxView = (CheckBox)findViewById(R.id.spectatorCheckboxView);
        lobbyList.setClickable(true);
        listGames();

        if(GameSettings.getLastPlayTime()!=-1){// A game has been played, push new playtime to server
            dataServer.sendRequest(
                    ServerCommand.SET_PLAYTIME,
                    ImmutableMap.of(
                            "id", String.valueOf(GameSettings.getUserId()),
                            "token", GameSettings.getPlayerToken(),
                            "playtime", String.valueOf(GameSettings.getProfile().getPlaytime()+GameSettings.getLastPlayTime())),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {}
                    });
            GameSettings.resetLastPlaytime();
        }
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

    private void listGames() {
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
                        LobbyListItem clickedItem = listOfRooms.get(position);
                        joinGame(clickedItem);
                    }
                });
            }
        });
    }

    public void showHostingActivity(View view) {
        if (!checkBoxView.isChecked()) {
            gameListUpdater.cancel();
            startActivity(new Intent(this, HostingActivity.class));
        }
        else {
            showToast(R.string.hostingError);
        }
    }

    private void showRoomActivity() {
        gameListUpdater.cancel();
        startActivity(new Intent(this, RoomActivity.class));
    }

    public void createLobby(JSONArray result) {

        listOfRooms = new ArrayList<>();
        roomIds = new HashMap<>();

        try {
            for(int i = 0; i < result.length(); i++) {
                JSONObject newRoom = result.getJSONObject(i);
                LobbyListItem item = new LobbyListItem(
                        newRoom.getString("name"),
                        newRoom.getString("ownerName"),
                        newRoom.getInt("owner"),
                        newRoom.getInt("maxPlayers"),
                        newRoom.getInt("playerCount"),
                        (newRoom.getInt("canBreakWall") == 1),
                        newRoom.getInt("timeLimit"),
                        newRoom.getDouble("maxDist")
                );
                listOfRooms.add(item);
                roomIds.put(newRoom.getString("name"),newRoom.getInt("id"));
                if(gameToJoin==newRoom.getInt("id")) {
                    joinGame(item);
                    gameToJoin = -1;// Reset gameToJoin so when we return to lobby we don't join it again
                }
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

    private void showToast(@StringRes int resId, Object... args) {
        String text = getString(resId);
        if(args.length>0)
            text = String.format(text, args);
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void joinGame(LobbyListItem item){
        if (item.getPlayerCount() > item.getMaxPlayersAsInteger()) {
            Toast.makeText(this, getString(R.string.too_many_players), Toast.LENGTH_SHORT).show();
            return;
        }

        String gameName = item.getGamename();

        GameSettings.setGameName(gameName);
        GameSettings.setGameId(roomIds.get(gameName));
        GameSettings.setOwnerId(item.getHostId());
        GameSettings.setCanBreakWall(item.getCanBreakWall());
        GameSettings.setTimeLimit(item.getTimeLimit());
        GameSettings.setMaxDistance(item.getMaxDist());
        GameSettings.setMaxPlayers(item.getMaxPlayersAsInteger());
        GameSettings.setSpectate(checkBoxView.isChecked());
        showToast(R.string.joinging_message, gameName);

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

}
