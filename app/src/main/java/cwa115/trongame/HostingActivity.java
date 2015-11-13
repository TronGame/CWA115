package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        final String query =
                "insertGame?owner="
                + Integer.toString(GameSettings.getPlayerId())
                + "&name=" + gameName
                + "&token=" + GameSettings.getPlayerToken()
                + "&maxPlayers=25";
        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    GameSettings.setGameToken(result.getString("token"));
                    GameSettings.setGameId(result.getInt("id"));
                    GameSettings.setGameName(gameName);
                    GameSettings.setIsOwner(true);
                    startActivity(new Intent(HostingActivity.this, RoomActivity.class));
                } catch(JSONException e) {
                    Toast.makeText(
                            getBaseContext(), getString(R.string.hosting_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

    }
}
