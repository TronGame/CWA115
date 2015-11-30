package cwa115.trongame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;

public class ScoreBoardActivity extends AppCompatActivity {
    private  HttpConnector dataServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board_actyvity);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        listScoreboard();
    }

    protected void listScoreboard(){
        dataServer.sendRequest(ServerCommand.SCOREBOARD, null, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try{
                    JSONArray result = new JSONArray(data);
                    for (int i=0; i==result.length(); i++){
                        JSONObject currentJSONObject = result.getJSONObject(i);
                    }
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
