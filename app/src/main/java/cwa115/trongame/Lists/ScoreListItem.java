package cwa115.trongame.Lists;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;
import cwa115.trongame.Profile;
import cwa115.trongame.R;

public class ScoreListItem {
    private Profile player;
    private int gamesWon;
    private HttpConnector dataServer;


    public ScoreListItem(String id, int wins) {
        gamesWon = wins;
        dataServer.sendRequest(ServerCommand.SHOW_ACCOUNT, ImmutableMap.of("id",id), new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    if (!result.has("error")) {
                        player = new Profile(
                                null, null,
                                result.getString("name"),
                                result.getString("pictureUrl"),
                                null
                        );
                    } else {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public String getPlayerPictureUrl(){
        return player.getPictureUrl();
    }

    public Profile getProfile(){
        return player;
    }
}

