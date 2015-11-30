package cwa115.trongame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Lists.ScoreCustomAdapter;
import cwa115.trongame.Lists.ScoreListItem;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;

public class ScoreBoardActivity extends AppCompatActivity {
    private static final int SCORE_BOARD_REFRESH_TIME = 5000;
    private HttpConnector dataServer;
    private Timer scoreBoardUpdater;
    private Handler scoreBoardHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
    }

    @Override
    protected void onResume() {
        super.onResume();
        scoreBoardUpdater = new Timer();
        scoreBoardHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                displayScoreboard();
            }
        };
        scoreBoardUpdater.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                scoreBoardHandler.sendMessage(new Message());
            }
        }, 0, SCORE_BOARD_REFRESH_TIME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        scoreBoardUpdater.cancel();
    }


    private void displayScoreboard() {
        dataServer.sendRequest(ServerCommand.SCOREBOARD, null, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                List<ScoreListItem> scoreList = new ArrayList<ScoreListItem>();
                try{
                    JSONArray result = new JSONArray(data);
                    for(int i = 0; i < result.length(); ++i) {
                        JSONObject playerRecord = result.getJSONObject(i);
                        scoreList.add(new ScoreListItem(
                                playerRecord.getString("name"), playerRecord.getInt("gamesWon")
                        ));
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                ListView lobbyList = (ListView) findViewById(R.id.score_list);
                ScoreCustomAdapter adapter = new ScoreCustomAdapter(ScoreBoardActivity.this, scoreList);
                lobbyList.setAdapter(adapter);
            }
        });
    }
}
