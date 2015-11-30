package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.HttpConnector;

public class HostingActivity extends AppCompatActivity {

    HttpConnector dataServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosting);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

    }
    public void showRoomActivity(View view) {
        EditText nameBox = (EditText) findViewById(R.id.game_name);
        final String gameName = nameBox.getText().toString();
        final int maxPlayers = getMaxPlayers();
        GameSettings.setMaxPlayers(maxPlayers);
        CheckBox breakWallBox = (CheckBox) findViewById(R.id.checkBoxWallBreaker);
        final boolean canBreakWalls = breakWallBox.isChecked();
        final String query =
                "insertGame?owner="
                + Integer.toString(GameSettings.getUserId())
                + "&name=" + gameName
                + "&token=" + GameSettings.getPlayerToken()
                + "&maxPlayers=" + Integer.toString(maxPlayers)
                + "&canBreakWall="+ (canBreakWalls ? "1" : "0");
        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    GameSettings.setGameToken(result.getString("token"));
                    GameSettings.setGameId(result.getInt("id"));
                    GameSettings.setGameName(gameName);
                    GameSettings.setOwnerId(GameSettings.getUserId());
                    GameSettings.setCanBreakWall(canBreakWalls);
                    GameSettings.setTimelimit(getTimeLimit());
                    joinOwnGame();
                } catch(JSONException e) {
                    Toast.makeText(
                            getBaseContext(), getString(R.string.hosting_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

    }

    private void joinOwnGame(){
        String dataToSend = "joinGame?gameId=" + Integer.toString(GameSettings.getGameId())
                +"&id="+ Integer.toString(GameSettings.getUserId())
                +"&token="+GameSettings.getPlayerToken();
        dataServer.sendRequest(dataToSend, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                startActivity(new Intent(HostingActivity.this, RoomActivity.class));
            }
        });
    }
    private int getMaxPlayers(){
        EditText editMaxPlayers = (EditText)findViewById(R.id.maxPlayers);
        String maxPlayers = editMaxPlayers.getText().toString();
        if (maxPlayers.length() == 0)
            return 1;
        else
            return Integer.parseInt(maxPlayers);

    }
    private int getTimeLimit(){
        EditText editTimeLimit = (EditText) findViewById(R.id.editTimeLimit);
        String timeLimit = editTimeLimit.getText().toString();
        if (timeLimit.length() == 0)
            return -1;
        else
            return Integer.parseInt(timeLimit);
    }
}
