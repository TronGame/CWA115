package cwa115.trongame.Network;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Profile;

/**
 * Created by Bram on 27-11-2015.
 */
public class FacebookRequest {

    public interface Callback{
        void handleResult(Profile profile);
    }

    private FacebookRequest(){}

    public static void sendRequest(final AccessToken facebookToken, final Callback callback){
        /* make the API call */
        Bundle params = new Bundle();
        params.putString("fields", "name,picture,friends");
        new GraphRequest(
                facebookToken,
                "/" + facebookToken.getUserId(),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject userData = response.getJSONObject();
                            // Get the user's name
                            String name = userData.getString("name");
                            // Get the user's profile picture url, which is stored inside a JSONObject
                            // named picture, which contains a JSONObject named data, which contains
                            // a string named url.
                            String profilePictureUrl = userData.getJSONObject("picture").getJSONObject("data").getString("url");
                            // Get the user's friends, which is stored inside a JSONObject named friends,
                            // which contains a JSONArray named data
                            JSONArray friends = userData.getJSONObject("friends").getJSONArray("data");
                            // We loop over this array which contains ids and names of the user's friends
                            // We store the ids in a separate Long[] array.
                            Long[] facebookIds = new Long[friends.length()];
                            for(int i=0;i<friends.length();i++){
                                facebookIds[i] = friends.getJSONObject(i).getLong("id");
                            }
                            // Give retrieved data to callback
                            Profile facebookProfile = new Profile(name, profilePictureUrl, facebookIds);
                            facebookProfile.setFacebookId(Long.parseLong(facebookToken.getUserId()));
                            callback.handleResult(facebookProfile);
                        }catch(JSONException e){
                            Log.e("FACEBOOK_REQUEST", "An exception occurred while trying to retrieve the user's data.");
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }
}
