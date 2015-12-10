package cwa115.trongame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.User.Profile;
import cwa115.trongame.Utils.PopUp;

public class RoomActivity extends AppCompatActivity
        implements PopUp.NoticeDialogListener{

    public final static String FROM_ROOMACTIVITY_EXTRA = "roomActivity_fromRoomActivityExtra";

    private final static int ROOM_LIST_REFRESH_TIME = 1000;
    private final static int FRIEND_LIST_REQUEST_CODE = 1;
    private final static int PROFILE_REQUEST_CODE = 2;

    private HttpConnector dataServer;
    public static Timer roomUpdater;
    private Handler roomHandler;
    private List<Integer> listOfColors;
    private boolean hasStarted, keepUpdating;
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
        Button startButton = (Button)findViewById(R.id.readyButton);
        if (!GameSettings.isOwner())
            startButton.setVisibility(View.INVISIBLE);


        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        listOfColors = colorListMaker();
        roomUpdater = new Timer();
        hasStarted = false;
        keepUpdating = false;
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(roomHandler==null)
            roomHandler = new Handler() {
                public void handleMessage(Message msg) {
                    listPlayers();
                }
            };
        roomUpdater.cancel();
        roomUpdater = new Timer();
        roomUpdater.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    roomHandler.sendMessage(new Message());
                }
        }, 0, ROOM_LIST_REFRESH_TIME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!keepUpdating)// Keep updating if we go to FriendsList or ProfileActivity
            roomUpdater.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==FRIEND_LIST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<Integer> selectedIds = data.getIntegerArrayListExtra(FriendsListActivity.SELECTED_IDS_EXTRA);
                dataServer.sendRequest(
                        ServerCommand.ADD_INVITE,
                        ImmutableMap.of(
                                "id", String.valueOf(GameSettings.getUserId()),
                                "token", GameSettings.getPlayerToken(),
                                "friends", new JSONArray(selectedIds).toString(),
                                "gameId", String.valueOf(GameSettings.getGameId())),
                        new HttpConnector.Callback() {
                            @Override
                            public void handleResult(String data) {
                                try{
                                    JSONObject result = new JSONObject(data);
                                    if(result.getBoolean("success"))
                                        showToast(R.string.friends_invited);
                                }catch (JSONException e){
                                    e.printStackTrace();
                                    showToast(R.string.friends_invite_error);
                                }
                            }
                        });
            }
            keepUpdating = false;
        }else if(requestCode==PROFILE_REQUEST_CODE){
            keepUpdating = false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Check if this device has the game token
            if (GameSettings.getGameToken() != null){
                Map<String, String> query = ImmutableMap.of("token", GameSettings.getGameToken(), "id", String.valueOf(GameSettings.getGameId()));
                dataServer.sendRequest(ServerCommand.DELETE_GAME, query, new HttpConnector.Callback() {
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
        Log.d("RoomActivity", "listPlayers called");
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
                            boolean containsSelf = false;
                            for (int i = 0; i < players.length(); i++) {
                                JSONObject player = players.getJSONObject(i);
                                int playerId = player.getInt("id");
                                listOfPlayerNames.add(new RoomListItem(
                                        player.getString("name"), listOfColors.get(i), playerId
                                ));
                                containsSelf = containsSelf || (playerId == GameSettings.getUserId());
                            }
                            if (!containsSelf && !GameSettings.getSpectate()) {
                                safeExit();
                            }

                            ListView lobbyList = (ListView) findViewById(R.id.room_list);
                            int ownerId = result.getInt("ownerId");
                            RoomCustomAdapter adapter = new RoomCustomAdapter(RoomActivity.this, listOfPlayerNames, ownerId, new RoomCustomAdapter.Callback() {
                                @Override
                                public void OnPlayerKick(int playerId, String playerName) {
                                    selectedPlayerName = playerName;
                                    selectedPlayerId = playerId;
                                    if(playerId == GameSettings.getUserId())
                                        ownerKick();// This won't happen anymore though
                                    else
                                        showNoticeDialog();
                                }
                            });
                            lobbyList.setAdapter(adapter);
                            lobbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
                                    RoomListItem clickedItem = listOfPlayerNames.get(position);
                                    selectedPlayerName = clickedItem.getPlayerName();

                                    keepUpdating = true;
                                    Bundle data = new Bundle();
                                    data.putParcelable(ProfileActivity.PROFILE_EXTRA, new Profile(clickedItem.getPlayerId(), null));
                                    data.putBoolean(FROM_ROOMACTIVITY_EXTRA, true);

                                    Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.DATA_EXTRA, data);

                                    startActivityForResult(intent, PROFILE_REQUEST_CODE);
                                }
                            });
                        } catch (JSONException e) {
                            gameDeleted();
                        }
                    }
                });

    }

    public void ownerKick() {
        Toast.makeText(this, R.string.owner_kick, Toast.LENGTH_SHORT).show();
    }

    public void gameDeleted() {
        Toast.makeText(this, getString(R.string.game_deleted), Toast.LENGTH_SHORT).show();
        safeExit(); // Probably game was deleted
    }

    public void safeExit(){
        StaticSafeExit(dataServer);
        finish();
    }
    public static void StaticSafeExit(HttpConnector dataServer) {
        GameSettings.setGameId(0);
        GameSettings.setGameName(null);
        GameSettings.setGameToken(null);
        GameSettings.setOwnerId(0);
        GameSettings.setCanBreakWall(false);
        GameSettings.setTimeLimit(-1);
        GameSettings.setMaxDistance(-1);
        GameSettings.setSpectate(false);
        if (!GameSettings.getSpectate()) {
            // Tell the server that you have left
            ImmutableMap query = ImmutableMap.of(
                    "playerId", GameSettings.getPlayerId(),
                    "token", GameSettings.getPlayerToken());

            dataServer.sendRequest(ServerCommand.LEAVE_GAME, query, new HttpConnector.Callback() {
                @Override
                public void handleResult(String data) {
                    try {
                        JSONObject result = new JSONObject(data);
                        // TODO check for errors

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (GameSettings.getGameToken() != null) {
            // The owner has left so the game has to be deleted
            ImmutableMap query = ImmutableMap.of(
                    "gameId", String.valueOf(GameSettings.getGameId()),
                    "token", GameSettings.getGameToken());

            dataServer.sendRequest(ServerCommand.DELETE_GAME, query, new HttpConnector.Callback() {
                @Override
                public void handleResult(String data) {
                    try {
                        JSONObject result = new JSONObject(data);
                        // TODO check for errors

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
        bundle.putString(PopUp.BUNDLE_MESSAGE_KEY, String.format(getString(R.string.kickRequest), selectedPlayerName));
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

                            ArrayList<Integer> listOfPlayerIds = new ArrayList<>();
                            for (int i = 0; i < players.length(); i++) {
                                JSONObject player = players.getJSONObject(i);
                                int playerId = player.getInt("id");
                                listOfPlayerIds.add(playerId);
                                if (player.getInt("id") == GameSettings.getUserId())
                                    GameSettings.setWallColor(listOfColors.get(i));
                                // If player is friend of current user, increase commonPlays
                                dataServer.sendRequest(
                                        ServerCommand.INCREASE_COMMON_PLAYS,
                                        ImmutableMap.of(
                                                "id", GameSettings.getPlayerId(),
                                                "token", GameSettings.getPlayerToken(),
                                                "friendId", String.valueOf(playerId)
                                        ),
                                        new HttpConnector.Callback() {
                                            @Override
                                            public void handleResult(String data) {
                                            }
                                        });
                            }
                            GameSettings.setPlayersInGame(listOfPlayerIds);
                            startGame();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void onTooManyPlayers() {
        Toast.makeText(this, R.string.too_many_players, Toast.LENGTH_SHORT).show();
    }

    public void inviteFriends(View v){
        Bundle data = new Bundle();
        data.putString(FriendsListActivity.TITLE_EXTRA,"Select friends to invite:");
        data.putBoolean(FriendsListActivity.SELECTABLE_EXTRA, true);
        data.putString(FriendsListActivity.COMMIT_TEXT_EXTRA, "Send invite");
        data.putParcelable(FriendsListActivity.PROFILE_EXTRA, GameSettings.getProfile());

        Intent intent = new Intent(this, FriendsListActivity.class);
        intent.putExtra(FriendsListActivity.DATA_EXTRA, data);

        keepUpdating = true;
        startActivityForResult(intent, FRIEND_LIST_REQUEST_CODE);
    }

    private void startGame() {
        roomUpdater.cancel();

        if (GameSettings.getGameToken() != null) {
            dataServer.sendRequest(
                ServerCommand.START_GAME,
                ImmutableMap.of("token", GameSettings.getGameToken()),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        // TODO check for errors
                        showGameActivity();
                    }
                }
            );
        } else {
            showGameActivity();
        }
    }

    private void showGameActivity() {
        if(hasStarted)
            return;
        hasStarted = true;
        startActivity(new Intent(this, GameActivity.class));
    }

    private void showToast(@StringRes int resId){
        Toast.makeText(this,resId,Toast.LENGTH_SHORT).show();
    }

}
