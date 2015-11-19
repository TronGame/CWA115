package cwa115.trongame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.RoomCustomAdapter;
import cwa115.trongame.Lists.RoomListItem;
import cwa115.trongame.Network.HttpConnector;

public class RoomActivity extends AppCompatActivity {

    final static int ROOM_LIST_REFRESH_TIME = 1000;

    private HttpConnector dataServer;
    private Timer roomUpdater;
    private Handler roomHandler;
    private List<Integer> listOfColors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        final TextView roomname = (TextView)findViewById(R.id.roomname);
        roomname.setText(GameSettings.getGameName());
        listOfColors = colorListMaker();
        roomUpdater = new Timer();
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

    public void listPlayers(){

        String query = "showGame?gameId=" + GameSettings.getGameId();

        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                List<RoomListItem> listOfPlayerNames = new ArrayList<>();
                try {
                    JSONObject result = new JSONObject(data);
                    JSONArray players = result.getJSONArray("players");
                    for (int i = 0; i < players.length(); i++) {
                        JSONObject player = players.getJSONObject(i);
                        listOfPlayerNames.add(new RoomListItem(player.getString("name"),listOfColors.get(i)));
                    }
                } catch (JSONException e) {
                }
                ListView lobbyList = (ListView) findViewById(R.id.room_list);
                RoomCustomAdapter adapter = new RoomCustomAdapter(RoomActivity.this, listOfPlayerNames);
                lobbyList.setAdapter(adapter);
            }
        });

    }

    public List<Integer> colorListMaker() {
        int maxPlayers=GameSettings.getMaxPlayers();
        int numberOfTints = (int) Math.ceil(Math.cbrt(maxPlayers));
        int colorIndent= (int) Math.floor(256. / numberOfTints);
        List<Integer> colorList = new ArrayList<>();
        for (int red = 0; red < numberOfTints; red+=colorIndent) {
            for (int green = 0; green < numberOfTints; green+=colorIndent) {
                for (int blue = 0; blue < numberOfTints; blue+=colorIndent) {
                    int new_color=Color.argb(255, red, green, blue);
                    colorList.add(new_color);
                    if(colorList.size() == maxPlayers)
                        return colorList;
                }
            }
        }
        return colorList;
    }
    public void showGameActivity(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }

}
