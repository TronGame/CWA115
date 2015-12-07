package cwa115.trongame.User;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.R;

/**
 * Created by Bram on 7-12-2015.
 */
public final class Updater {

    private Updater(){ }

    public interface Callback{
        public void onDataUpdated();
        public void onProfileNotFound();
        public void onError();
    }

    /***
     * This method downloads the latest userdata from the server and stores it locally.
     */
    public static void updateServerUserData(HttpConnector dataServer, final SharedPreferences settings, final Callback callback){
        if(GameSettings.getProfile()==null || GameSettings.getProfile().getId()==null || GameSettings.getProfile().getToken()==null)
            return;
        dataServer.sendRequest(
                ServerCommand.SHOW_ACCOUNT,
                GameSettings.getProfile().GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (!result.has("error")) {
                                new Profile(
                                        null,
                                        null,
                                        null,
                                        result.getString("name"),
                                        result.getString("pictureUrl"),
                                        result.getInt("wins"),
                                        result.getInt("losses"),
                                        result.getInt("highscore"),
                                        result.getInt("playtime"),
                                        new FriendList(result.getJSONArray("friends"))
                                ).Store(settings);
                                if(callback!=null)
                                    callback.onDataUpdated();
                            } else if(callback!=null)
                                callback.onProfileNotFound();
                        } catch (JSONException e) {
                            if(callback!=null)
                                callback.onError();
                        }
                    }
                });
    }
}
