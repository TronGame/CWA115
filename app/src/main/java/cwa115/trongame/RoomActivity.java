package cwa115.trongame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.RoomCustomAdapter;
import cwa115.trongame.Lists.RoomListItem;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;
import cwa115.trongame.PopUp.PopUp;

public class RoomActivity extends AppCompatActivity
        implements PopUp.NoticeDialogListener{

    final static int ROOM_LIST_REFRESH_TIME = 1000;

    private HttpConnector dataServer;
    private Timer roomUpdater;
    private Handler roomHandler;
    private List<Integer> listOfColors;
    private boolean hasStarted;
    private int selectedPlayerId;
    private String selectedPlayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // Set the room name
        final TextView roomname = (TextView)findViewById(R.id.roomname);
        roomname.setText(GameSettings.getGameName());

        // Only the owner can start the game
        final Button startButton = (Button)findViewById(R.id.readyButton);
        if (!GameSettings.isOwner())
            startButton.setVisibility(View.GONE);

        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        listOfColors = colorListMaker();
        roomUpdater = new Timer();
        hasStarted = false;
    }


    @Override
    protected void onResume(){
        super.onResume();
        roomHandler = new Handler() {
            public void handleMessage(Message msg) {
                listPlayers();
            }
        };
        roomUpdater.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                roomHandler.sendMessage(new Message());
            }
        }, 0, ROOM_LIST_REFRESH_TIME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        roomUpdater.cancel();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (GameSettings.getUserId() == GameSettings.getOwner()){
                String query = "deleteGame?token=" + GameSettings.getGameToken() + "&id="+ GameSettings.getGameId();
                dataServer.sendRequest(query, new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {}
                });
            }
            safeExit();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void listPlayers(){

        dataServer.sendRequest(
                ServerCommand.SHOW_GAME,
                ImmutableMap.of("gameId", String.valueOf(GameSettings.getGameId())),
                new HttpConnector.Callback() {
                @Override
                public void handleResult(String data) {
                    final List<RoomListItem> listOfPlayerNames = new ArrayList<>();
                    try {
                        JSONObject result = new JSONObject(data);
                        if (result.getBoolean("hasStarted")) {
                            roomUpdater.cancel();
                            gameReady(null);
                            return;
                        }
                        JSONArray players = result.getJSONArray("players");
                        for (int i = 0; i < players.length(); i++) {
                            JSONObject player = players.getJSONObject(i);
                            listOfPlayerNames.add(new RoomListItem(player.getString("name"), listOfColors.get(i), player.getInt("id")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONArray players = result.getJSONArray("players");
                    boolean containsSelf = false;
                    for (int i = 0; i < players.length(); i++) {
                        JSONObject player = players.getJSONObject(i);
                        int playerId = player.getInt("id");
                        listOfPlayerNames.add(new RoomListItem(
                                player.getString("name"), listOfColors.get(i), playerId
                        ));
                        containsSelf = containsSelf || (playerId == GameSettings.getUserId());
                    }
                    if(!containsSelf) {
                        safeExit();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    safeExit(); // Probably game was deleted
                }
                ListView lobbyList = (ListView) findViewById(R.id.room_list);
                RoomCustomAdapter adapter = new RoomCustomAdapter(RoomActivity.this, listOfPlayerNames);
                lobbyList.setAdapter(adapter);

                lobbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
                        RoomListItem clickedItem = listOfPlayerNames.get(position);
                        selectedPlayerName= clickedItem.getPlayerName();
                        selectedPlayerId=clickedItem.getPlayerId();
                        showNoticeDialog();
                    }
                });
            }
        });

    }

    public void safeExit() {
        GameSettings.setGameId(0);
        GameSettings.setGameName(null);
        GameSettings.setGameToken(null);
        GameSettings.setOwnerId(0);
        GameSettings.setCanBreakWall(false);
        GameSettings.setTimelimit(-1);
        finish();
    }

    public List<Integer> colorListMaker() {
        int maxPlayers=GameSettings.getMaxPlayers();
        int numberOfTints = (int) Math.ceil(Math.cbrt(maxPlayers));
        int colorIndent= (int) Math.floor(256. / numberOfTints);
        List<Integer> colorList = new ArrayList<>();
        for(int red = 0; red < 256; red += colorIndent) {
            for(int green = 0; green < 256; green += colorIndent) {
                for(int blue = 0; blue < 256; blue += colorIndent) {
                    colorList.add(Color.rgb(red, green, blue));
                    if(colorList.size() == maxPlayers)
                        return colorList;
                }
            }
        }
        return colorList;
    }
    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new PopUp();
        Bundle bundle = new Bundle();
        bundle.putString(PopUp.BUNDLE_MESSAGE_KEY, getString(R.string.kickRequest) + selectedPlayerName + "?");
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Map<String, String> query = ImmutableMap.of(
                "playerId", Integer.toString(selectedPlayerId),
                "gameId", String.valueOf(GameSettings.getGameId()),
                "token", GameSettings.getGameToken());

        dataServer.sendRequest(ServerCommand.KICK_PLAYER, query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    if (result.getBoolean("success")) {
                        Toast.makeText(getBaseContext(), getString(R.string.playerKickSucceed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.playerKickFail), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    public void gameReady(View view) {

        dataServer.sendRequest(
                ServerCommand.SHOW_GAME,
                ImmutableMap.of("gameId", String.valueOf(GameSettings.getGameId())),
                new HttpConnector.Callback() {
                @Override
                public void handleResult(String data) {
                    try {
                        JSONObject result = new JSONObject(data);
                        JSONArray players = result.getJSONArray("players");
                        List<Integer> listOfPlayerIds = new ArrayList<>();
                        for (int i = 0; i < players.length(); i++) {
                            JSONObject player = players.getJSONObject(i);
                            listOfPlayerIds.add(player.getInt("id"));
                            // TODO make sure that this happens more reliably (the server might have to store player color as well?)
                            if (player.getInt("id") == GameSettings.getUserId())
                                GameSettings.setWallColor(listOfColors.get(i));
                        }
                        GameSettings.setPlayersInGame(listOfPlayerIds);
                        startGame();
                        showGameActivity();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        });
    }

    private void startGame() {
        roomUpdater.cancel();

        dataServer.sendRequest(
                ServerCommand.START_GAME,
                ImmutableMap.of("token", GameSettings.getGameToken()),
                new HttpConnector.Callback() {
                @Override
                public void handleResult(String data) {
                    // TODO check for errors
                    showGameActivity();
                }
        });
    }

    private void showGameActivity() {
        if(hasStarted)
            return;
        hasStarted = true;
        startActivity(new Intent(this, GameActivity.class));
    }

}
