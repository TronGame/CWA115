package cwa115.trongame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.Lists.ScoreCustomAdapter;
import cwa115.trongame.Lists.ScoreListItem;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;

public class ScoreBoardActivity extends AppCompatActivity {
    private  HttpConnector dataServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        listScoreboard();
    }

    protected void listScoreboard(){
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
