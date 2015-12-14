package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;

public class HostingActivity extends AppCompatActivity {

    public final static int MAX_PLAYERS = 100;
    HttpConnector dataServer;
    private boolean createdGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosting);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

    }
    public void showRoomActivity(View view) {
        if (createdGame)
            return;

        if (getMaxPlayers() > MAX_PLAYERS) {
            Toast.makeText(this, getString(R.string.over_max_players), Toast.LENGTH_SHORT).show();
            return;
        }

        createdGame = true;

        EditText nameBox = (EditText) findViewById(R.id.game_name);
        final String gameName = nameBox.getText().toString();
        final int maxPlayers = getMaxPlayers();
        GameSettings.setMaxPlayers(maxPlayers);
        CheckBox breakWallBox = (CheckBox) findViewById(R.id.checkBoxWallBreaker);
        final boolean canBreakWalls = breakWallBox.isChecked();
        Map<String, String> query = new HashMap<>();

        query.put("owner", Integer.toString(GameSettings.getUserId()));
        query.put("name", gameName);
        query.put("token", GameSettings.getPlayerToken());
        query.put("maxPlayers", Integer.toString(maxPlayers));
        query.put("canBreakWall", (canBreakWalls ? "1" : "0"));
        query.put("timeLimit", Integer.toString(getTimeLimit()));
        query.put("maxDist", Double.toString(getMaxDist()));

        //query = ImmutableMap.copyOf(query);
        //^^ query can be any Map<String, String> , an ImmutableMap is not required

        dataServer.sendRequest(
                ServerCommand.INSERT_GAME,
                query,
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            GameSettings.setGameToken(result.getString("token"));
                            GameSettings.setGameId(result.getInt("id"));
                            GameSettings.setGameName(gameName);
                            GameSettings.setOwnerId(GameSettings.getUserId());
                            GameSettings.setCanBreakWall(canBreakWalls);
                            GameSettings.setTimeLimit(getTimeLimit());
                            GameSettings.setMaxDistance(getMaxDist());
                            joinOwnGame();
                        } catch (JSONException e) {
                            Toast.makeText(
                                    getBaseContext(), getString(R.string.hosting_failed),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });

    }

    private void joinOwnGame(){
        Map<String, String> dataToSend = ImmutableMap.of(
                "gameId", Integer.toString(GameSettings.getGameId()),
                "id", Integer.toString(GameSettings.getUserId()),
                "token", GameSettings.getPlayerToken());
        dataServer.sendRequest(ServerCommand.JOIN_GAME, dataToSend, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                startActivity(new Intent(HostingActivity.this, RoomActivity.class));
            }
        });
    }
    private int getMaxPlayers(){
        EditText editMaxPlayers = (EditText)findViewById(R.id.maxPlayers);
        String maxPlayers = editMaxPlayers.getText().toString();
        if (maxPlayers.length() <= 0)
            return 2;
        else
            return Integer.parseInt(maxPlayers);

    }
    private int getTimeLimit(){
        EditText editTimeLimit = (EditText) findViewById(R.id.editTimeLimit);
        String timeLimit = editTimeLimit.getText().toString();
        if (timeLimit.equals(""))
            return -1;
        else
            return Integer.parseInt(timeLimit);
    }

    private double getMaxDist() {
        EditText editMaxDist = (EditText) findViewById(R.id.maxDist);
        String maxDist = editMaxDist.getText().toString();
        if (maxDist.equals(""))
            return -1;
        else
            return Double.parseDouble(maxDist);
    }

    @Override
    public void onResume() {
        super.onResume();
        createdGame = false;
    }
}
