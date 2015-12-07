package cwa115.trongame.User;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.games.Game;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.R;

/**
 * Updater class will be removed very soon, all methods are now available as static methods in Profile class
 */
public final class Updater {

    private Updater(){ }

    public interface Callback{
        public void onDataUpdated(Profile profile);
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
                                Profile profile = new Profile(
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
                                );
                                profile.Store(settings);
                                if(callback!=null)
                                    callback.onDataUpdated(profile);
                            } else if(callback!=null)
                                callback.onProfileNotFound();
                        } catch (JSONException e) {
                            if(callback!=null)
                                callback.onError();
                        }
                    }
                });
    }

    /***
     * This method downloads the latest userdata from the server and stores it locally.
     */
    public static void updateProfile(HttpConnector dataServer, final Profile profile, final Callback callback){
        if(profile==null || profile.getId()==null){
            if(callback!=null)
                callback.onError();
            return;
        }
        Map<String, String> query = profile.GetQuery(Profile.SERVER_ID_PARAM);
        if(profile.getToken()!=null)
            query.put(Profile.SERVER_TOKEN_PARAM, profile.getToken());
        dataServer.sendRequest(
                ServerCommand.SHOW_ACCOUNT,
                query,
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (!result.has("error")) {
                                long facebookId = result.optLong("facebookId",-1);
                                int playtime = -1;
                                FriendList friends = null;
                                if(profile.getToken()!=null){
                                    playtime = result.getInt("playtime");
                                    friends = new FriendList(result.getJSONArray("friends"));
                                }

                                Profile updatedProfile = new Profile(
                                        profile.getId(),
                                        profile.getToken(),
                                        facebookId==-1 ? null : facebookId,
                                        result.getString("name"),
                                        result.getString("pictureUrl"),
                                        result.getInt("wins"),
                                        result.getInt("losses"),
                                        result.getInt("highscore"),
                                        playtime==-1 ? null : playtime,
                                        friends
                                );
                                if(callback!=null)
                                    callback.onDataUpdated(updatedProfile);
                            } else if(callback!=null)
                                callback.onProfileNotFound();
                        } catch (JSONException e) {
                            if(callback!=null)
                                callback.onError();
                        }
                    }
                });
    }

    /***
     * This method downloads the latest userdata from the server and stores it locally.
     */
    public static void loadProfile(HttpConnector dataServer, int id, String token, final Callback callback){
        updateProfile(dataServer, new Profile(id, token), callback);
    }
}
