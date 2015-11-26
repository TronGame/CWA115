package cwa115.trongame;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.HttpConnector;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_HOME = 0;
    private final static int LOGIN_WELCOME = 1;

    private final static String ACCOUNT_NAME_KEY = "accountName";
    private final static String ACCOUNT_ID_KEY = "accountId";
    private final static String ACCOUNT_TOKEN_KEY = "accountToken";

    private CallbackManager callbackManager;
    private AccessToken facebookToken;
    private SharedPreferences settings;

    private HttpConnector dataServer;
    private boolean accountRegistered;

    private ViewFlipper loginViewFlipper;
    private Button roundButton;
    private TextView loginWelcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());

        // activity_main contains facebook login button
        setContentView(R.layout.activity_main);

        // Set the required permissions for the facebook login button
        LoginButton authButton = (LoginButton)findViewById(R.id.facebook_login_button);
        authButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));

        // Set the player marker that appears on the map
        GameSettings.setPlayerMarkerImage(R.mipmap.marker);

        // Create the server that controls the game management
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        settings = getPreferences(MODE_PRIVATE);
        showLoginOptions();

        if(!accountRegistered) {
            Button buttonDelete = (Button) findViewById(R.id.button);
            buttonDelete.setVisibility(Button.GONE);
        }
    }
        loginViewFlipper = (ViewFlipper)findViewById(R.id.login_view_flipper);
        roundButton = (Button)findViewById(R.id.round_button);
        loginWelcomeTextView = (TextView)findViewById(R.id.login_welcome_textview);

        if(settings.contains(ACCOUNT_NAME_KEY) && settings.contains(ACCOUNT_TOKEN_KEY))
            showWelcomeView();
        else
            showLoginView();
    }

    private void showWelcomeView() {
        roundButton.setText(getString(R.string.start));
        // User is already registered
        accountRegistered = true;
        facebookToken = AccessToken.getCurrentAccessToken();
        //if(isFacebookUser())
        //    updateFacebookUserData();// TODO: fix this: when user is logged in first time, the data shouldn't be updated

        String accountName = settings.getString(ACCOUNT_NAME_KEY, null);
        int accountId = settings.getInt(ACCOUNT_ID_KEY, 0);
        String accountToken = settings.getString(ACCOUNT_TOKEN_KEY, null);

        loginViewFlipper.setDisplayedChild(LOGIN_WELCOME);// Show welcome screen
        roundButton.setText(getString(R.string.start));
        loginWelcomeTextView.setText(String.format(getString(R.string.welcome_message), accountName));

        // TODO: check the correctness of the token

        GameSettings.setPlayerName(accountName);
        GameSettings.setUserId(accountId);
        GameSettings.setPlayerToken(accountToken);
    }

    public void resetAccountSettings(View view) {
        if(!accountRegistered)
            return;
        accountRegistered = false;

        deleteAccount(GameSettings.getUserId(), GameSettings.getPlayerToken());

        if(isFacebookUser())
            LoginManager.getInstance().logOut();
        facebookToken = null;// Reset token
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ACCOUNT_NAME_KEY);
        editor.remove(ACCOUNT_ID_KEY);
        editor.remove(ACCOUNT_TOKEN_KEY);
        editor.commit();
        showLoginView();
    }

    private void showLoginView() {
        roundButton.setText(getString(R.string.register));
        accountRegistered = false;
        loginViewFlipper.setDisplayedChild(LOGIN_HOME);

        // Enable facebook login
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookToken = loginResult.getAccessToken();
                Log.d("FACEBOOK_LOGIN", "Login succeeded: " + facebookToken.getUserId());
                // Load user data:
                loadFacebookUserData();
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK_LOGIN", "Login canceled");
                Toast.makeText(getBaseContext(), "Login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FACEBOOK_LOGIN", "Login error: " + error.toString());
                Toast.makeText(getBaseContext(), "Login error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        //SensorData.Pause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //SensorData.Resume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void loadFacebookUserData(){
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading userdata", "Please wait...", true, false);
        /* make the API call */
        Bundle params = new Bundle();
        params.putString("fields","name,picture,friends");
        new GraphRequest(
                facebookToken,
                "/" + facebookToken.getUserId(),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject userData = response.getJSONObject();
                            String name = userData.getString("name");// Get the user's name
                            // Get the user's profile picture url, which is stored inside a JSONObject
                            // named picture, which contains a JSONObject named data, which contains
                            // a string named url. This url is split at the question mark to dump
                            // metadata-arguments.
                            String profilePictureUrl = userData.getJSONObject("picture").getJSONObject("data").getString("url").split("\\?")[0];
                            // Get the user's friends, which is stored inside a JSONObject named friends,
                            // which contains a JSONArray named data
                            JSONArray friends = userData.getJSONObject("friends").getJSONArray("data");
                            // We loop over this array which contains ids and names of the user's friends
                            // We store the ids in a separate Long[] array.
                            Long[] facebookIds = new Long[friends.length()];
                            for(int i=0;i<friends.length();i++){
                                facebookIds[i] = friends.getJSONObject(i).getLong("id");
                            }
                            Log.d("FACEBOOK_LOGIN", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl + " ; friends: " + friends.toString());
                            // Then we register the user on our server
                            registerFacebookAccount(name, profilePictureUrl, facebookIds);
                            progressDialog.dismiss();
                        }catch(JSONException e){
                            Log.e("FACEBOOK_LOGIN", "An exception occurred while trying to retrieve the user's data.");
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void updateFacebookUserData(){
        /* make the API call */
        Bundle params = new Bundle();
        params.putString("fields","name,picture,friends");
        new GraphRequest(
                facebookToken,
                "/" + facebookToken.getUserId(),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject userData = response.getJSONObject();
                            String name = userData.getString("name");
                            String profilePictureUrl = userData.getJSONObject("picture").getJSONObject("data").getString("url");
                            JSONArray friends = userData.getJSONObject("friends").getJSONArray("data");
                            Long[] facebookIds = new Long[friends.length()];
                            for(int i=0;i<friends.length();i++){
                                facebookIds[i] = friends.getJSONObject(i).getLong("id");
                            }
                            Log.d("FACEBOOK_LOGIN", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl + " ; friends: " + friends.toString());
                            //TODO: push modified data to server
                        }catch(JSONException e){
                            Log.e("FACEBOOK_LOGIN", "An exception occurred while trying to retrieve the user's data.");
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void registerFacebookAccount(final String name, final String pictureUrl, final Long[] friends) throws JSONException{
        // First receive userIds of friends based on their facebookIds:
        final String query = "getFriendIds?facebookIds=" + new JSONArray(Arrays.asList(friends));
        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    registerAccount(name, pictureUrl, new JSONArray(result.getString("friends")), Long.parseLong(facebookToken.getUserId()));
                } catch (JSONException e) {
                    Toast.makeText(
                            getBaseContext(), getString(R.string.register_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void registerAccount(final String name, final String pictureUrl, final JSONArray friends, final Long facebookId) {
        String query = "name=" + name;
        if(pictureUrl!=null && !pictureUrl.isEmpty()) query += "&pictureUrl=" + pictureUrl;
        if(friends!=null && friends.length()!=0) query += "&friends=" + friends;
        if(facebookId!=null) query += "&facebookId=" + facebookId;
        try {
            // encode query (replace space by %20 and encode other special characters)
            URI uri = new URI(null,null,"insertAccount",query,null);
            query = uri.toString();
        }catch(URISyntaxException e){
            Log.e("REGISTER_ACCOUNT","Wrong parameters URI specified");
            // if encoding fails, make sure query contains at least the right command
            // this way a valid request can be made, although it will presumably fail because
            // it's not encoded right.
            query = "insertAccount?" + query;
        }
        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    String token = result.getString("token");
                    int identifier = result.getInt("id");
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(ACCOUNT_NAME_KEY, name);
                    editor.putInt(ACCOUNT_ID_KEY, identifier);
                    editor.putString(ACCOUNT_TOKEN_KEY, token);
                    editor.commit();
                    accountRegistered = true;
                    Button buttonStart = (Button) findViewById(R.id.start_button);
                    buttonStart.setText(getString(R.string.start));
                    buttonStart.setTextSize(60);
                    Button buttonDelete = (Button) findViewById(R.id.button);
                    buttonDelete.setVisibility(Button.VISIBLE);
                    Toast.makeText(
                            getBaseContext(), getString(R.string.account_created),
                            Toast.LENGTH_SHORT
                    ).show();
                    showWelcomeView();
                } catch(JSONException e) {
                    Toast.makeText(
                            getBaseContext(), getString(R.string.register_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void deleteAccount(final Integer id, String token) {
        final String query = "deleteAccount?id=" + id + ";token=" + token;
        dataServer.sendRequest(query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    String success = result.getString("success");
                    Button buttonStart = (Button) findViewById(R.id.start_button);
                    buttonStart.setText(getString(R.string.register));
                    Button buttonDelete = (Button) findViewById(R.id.button);
                    buttonDelete.setVisibility(Button.GONE);
                    if(success == "true") {
                        Toast.makeText(
                                getBaseContext(), getString(R.string.account_deleted),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    else {
                        Toast.makeText(
                                getBaseContext(), getString(R.string.delete_failed),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(
                            getBaseContext(), getString(R.string.delete_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    public boolean isFacebookUser(){
        return (facebookToken!=null);
    }

    public void showGameActivity(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        GameSettings.setPlayerName(nameBox.getText().toString());
        startActivity(new Intent(this, GameActivity.class));
    }

    public void showLobbyActivity(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        if(accountRegistered)
            startActivity(new Intent(this, LobbyActivity.class));
        else
            registerAccount(nameBox.getText().toString(),"",null,null);
    }

}
