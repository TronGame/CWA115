package cwa115.trongame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
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
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.FacebookRequest;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_HOME = 0;
    private final static int LOGIN_WELCOME = 1;

    private final static String ACCOUNT_NAME_KEY = "accountName";
    private final static String ACCOUNT_ID_KEY = "accountId";
    private final static String ACCOUNT_TOKEN_KEY = "accountToken";
    private final static String ACCOUNT_PICTURE_URL = "accountPictureUrl";
    private final static String ACCOUNT_FRIENDS = "accountFriends";

    private CallbackManager callbackManager;
    private AccessToken facebookToken;
    private SharedPreferences settings;

    private HttpConnector dataServer;
    private boolean accountRegistered;

    private ViewFlipper loginViewFlipper;
    private Button mainButton, deleteButton;
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

        // Load settings file
        settings = getPreferences(MODE_PRIVATE);

        // Create reference to important UI-elements
        loginViewFlipper = (ViewFlipper)findViewById(R.id.login_view_flipper);
        mainButton = (Button)findViewById(R.id.main_button);
        deleteButton = (Button)findViewById(R.id.delete_button);
        loginWelcomeTextView = (TextView)findViewById(R.id.login_welcome_textview);

        // TODO: check if user is connected to the internet and if GPS is turned on before proceeding

        // If a user is signed in, show welcome view, otherwise show login view
        if(settings.contains(ACCOUNT_ID_KEY) && settings.contains(ACCOUNT_TOKEN_KEY))
            showWelcomeView(true);
        else
            showLoginView();
    }

    private void showWelcomeView(boolean updateUserData) {
        // User is already registered
        accountRegistered = true;
        facebookToken = AccessToken.getCurrentAccessToken();

        // Load local stored user settings
        int accountId = settings.getInt(ACCOUNT_ID_KEY, 0);
        String accountName = settings.getString(ACCOUNT_NAME_KEY, null);
        String accountToken = settings.getString(ACCOUNT_TOKEN_KEY, null);

        // Update current userdata if requested
        /*if(updateUserData){
            if(isFacebookUser())
                updateFacebookUserData(accountId, accountToken);// updateFacebookUserData will automatically call updateServerUserData
            else
                updateServerUserData(accountId, accountToken);
            return;
        }*/

        // Update UI
        loginViewFlipper.setDisplayedChild(LOGIN_WELCOME);// Show welcome screen
        loginWelcomeTextView.setText(String.format(getString(R.string.welcome_message), accountName));
        mainButton.setText(getString(R.string.start));
        mainButton.setTextSize(60);
        deleteButton.setVisibility(View.VISIBLE);

        // TODO: check the correctness of the token

        // Store userdata in GameSettings
        GameSettings.setPlayerName(accountName);
        GameSettings.setUserId(accountId);
        GameSettings.setPlayerToken(accountToken);
    }

    private void showLoginView() {
        // User is not registered
        accountRegistered = false;

        // Update UI
        mainButton.setText(getString(R.string.register));
        mainButton.setTextSize(40);
        deleteButton.setVisibility(View.GONE);
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
                showToast("Login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FACEBOOK_LOGIN", "Login error: " + error.toString());
                showToast("Login error");
            }
        });
    }

    public void resetAccountSettings(View view) {
        if(!accountRegistered)// Make sure a user is actually signed in
            return;
        accountRegistered = false;// User isn't registered anymore

        // Delete user's account
        deleteAccount(GameSettings.getUserId(), GameSettings.getPlayerToken());

        if(isFacebookUser())
            LoginManager.getInstance().logOut();// Logout from facebook
        facebookToken = null;// Reset token

        // Delete saved user settings
        SharedPreferences.Editor editor = settings.edit();// TODO: Use editor.clear() instead?
        editor.remove(ACCOUNT_NAME_KEY);
        editor.remove(ACCOUNT_ID_KEY);
        editor.remove(ACCOUNT_TOKEN_KEY);
        editor.apply();
        showLoginView();
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

    /***
     * This method loads the facebook data when the user signs in with facebook. This data is used
     * to create an account on our server by calling registerFacebookAccount.
     */
    private void loadFacebookUserData() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading userdata", "Please wait...", true, false);
        FacebookRequest.sendRequest(facebookToken, new FacebookRequest.Callback() {
            @Override
            public void handleResult(String name, String profilePictureUrl, Long[] friends) {
                Log.d("FACEBOOK_LOGIN", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl + " ; friends: " + friends.toString());
                // Then we register the user on our server
                registerFacebookAccount(name, profilePictureUrl, friends);
                progressDialog.dismiss();
            }
        });
    }

    /***
     * This method gets the latest userdata from facebook and stores the modified data to our server.
     * Once the data is collected, it'll call updateUserData so the modified server data is also
     * stored locally. The method updateUserData will show the welcome view afterwards.
     * @param userId The id of the user whose data will be collected (necessary for the updateUserData call)
     * @param userToken The token of the user whose data will be collected (necessary for the updateUserData call)
     */
    private void updateFacebookUserData(final int userId, final String userToken) {
        FacebookRequest.sendRequest(facebookToken, new FacebookRequest.Callback() {
            @Override
            public void handleResult(String name, String profilePictureUrl, Long[] friends) {
                Log.d("FACEBOOK_UPDATE", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl + " ; friends: " + friends.toString());
                String query = "id=" + userId + "&token=" + userToken;
                String oldName=settings.getString(ACCOUNT_NAME_KEY,null);
                String oldPictureUrl=settings.getString(ACCOUNT_PICTURE_URL,null);
                String oldFriends=settings.getString(ACCOUNT_FRIENDS,null);
                String newFriends=new JSONArray(Arrays.asList(friends)).toString();
                if(!name.equals(oldName)) query += "&name=" + name;
                if(!profilePictureUrl.equals(oldPictureUrl)) query += "&pictureUrl=" + profilePictureUrl;
                if(!newFriends.equals(oldFriends)) query += "&friends=" + newFriends;

                dataServer.sendRequest(ServerCommand.UPDATE_ACCOUNT, query, new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (result.getBoolean("success"))
                                updateServerUserData(userId, userToken);
                            else
                                showToast(R.string.update_failed);
                        } catch (JSONException e) {
                            showToast(R.string.update_failed);
                        }
                    }
                });
            }
        });
    }

    /***
     * This method downloads the latest userdata from the server and stores it locally. Once the data
     * is collected, it'll show the welcome view to the user.
     * @param userId Id of the user whose data will be downloaded
     * @param userToken Token of the user whose data will be downloaded
     */
    private void updateServerUserData(int userId, String userToken){
        String query = "id=" + userId + "&token=" + userToken;
        dataServer.sendRequest(ServerCommand.SHOW_ACCOUNT, query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try{
                    JSONObject result = new JSONObject(data);
                    if(!result.has("error")) {
                        String name = result.getString("name");
                        String pictureUrl = result.getString("pictureUrl");
                        JSONArray friends = result.getJSONArray("friends");
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(ACCOUNT_NAME_KEY, name);
                        editor.putString(ACCOUNT_PICTURE_URL, pictureUrl);
                        editor.putString(ACCOUNT_FRIENDS, friends.toString());
                        editor.apply();
                        showWelcomeView(false);// Update UI
                    }else{
                        // User was not found on server
                        showToast("Profile not found");
                        // TODO: delete local account so a new one can be created?
                    }
                }catch(JSONException e){
                    showToast(R.string.update_failed);
                }
            }
        });
    }

    /**
     * This method registers a facebook user on our server. First all his friends' userIds are loaded
     * from our server. These friend ids are then sent to the registerAccount method along with the
     * previously loaded facebook data (name & pictureUrl).
     * @param name The user's name (loaded by loadFacebookUserData, sent to registerAccount)
     * @param pictureUrl The user's profilePictureUrl (loaded by loadFacebookUserData, sent to registerAccount)
     * @param friends An array containing the FACEBOOK-userIds of the user's friend (loaded by loadFacebookUserData)
     *                These ids are sent to our server. The server converts them to SERVER-userIds of the user's friends.
     *                These SERVER-userIds are then sent to registerAccount along with the other facebook data.
     */
    private void registerFacebookAccount(final String name, final String pictureUrl, final Long[] friends){
        // First receive userIds of friends based on their facebookIds:
        final String query = "facebookIds=" + new JSONArray(Arrays.asList(friends));
        dataServer.sendRequest(ServerCommand.GET_FRIEND_IDS, query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    registerAccount(name, pictureUrl, new JSONArray(result.getString("friends")), Long.parseLong(facebookToken.getUserId()));
                } catch (JSONException e) {
                    showToast(R.string.register_failed);
                }
            }
        });
    }

    /***
     * This method registers an account (local or facebook) on our server. If the account creation
     * is successful, the user's data is stored locally and the welcome view is displayed.
     * @param name The user's name
     * @param pictureUrl The user's profilePictureUrl (null if local)
     * @param friends The user's friends (null if local)
     * @param facebookId The user's facebookId (null if local)
     */
    private void registerAccount(final String name, final String pictureUrl, final JSONArray friends, final Long facebookId) {
        String query = "name=" + name;
        if(pictureUrl!=null && !pictureUrl.isEmpty()) query += "&pictureUrl=" + pictureUrl;
        if(friends!=null && friends.length()!=0) query += "&friends=" + friends;
        if(facebookId!=null) query += "&facebookId=" + facebookId;
        dataServer.sendRequest(ServerCommand.INSERT_ACCOUNT, query, new HttpConnector.Callback() {
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
                    editor.apply();
                    showToast(R.string.account_created);
                    showWelcomeView(false);
                } catch(JSONException e) {
                    showToast(R.string.register_failed);
                }
            }
        });
    }

    private void deleteAccount(final Integer id, String token) {
        final String query = "id=" + id + ";token=" + token;
        dataServer.sendRequest(ServerCommand.DELETE_ACCOUNT, query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    String success = result.getString("success");
                    if(success.equals("true")) {
                        showToast(R.string.account_deleted);
                        showLoginView();
                    }
                    else
                        showToast(R.string.delete_failed);

                } catch (JSONException e) {
                    showToast(R.string.delete_failed);
                }
            }
        });
    }

    /***
     * This method checks if a facebookToken is set.
     * @return A boolean determining whether a facebook user is signed in or not.
     */
    public boolean isFacebookUser(){
        return (facebookToken!=null);
    }

    /***
     * Shows a Toast with the specified text.
     * @param text The text to display inside the Toast.
     */
    private void showToast(String text){
        Toast.makeText(
                getBaseContext(),
                text,
                Toast.LENGTH_SHORT
        ).show();
    }

    /***
     * Shows a Toast with the specified text.
     * @param resId The resourceId of the text to display inside the Toast.
     */
    private void showToast(@StringRes int resId){
        showToast(getString(resId));
    }

    private String encodeServerCommand(String action, String query){
        try {
            // encode query (replace space by %20 and encode other special characters)
            URI uri = new URI(null,null,action,query,null);
            return uri.toString();
        }catch(URISyntaxException e){
            Log.e("URI_ENCODING","Wrong parameters URI specified. Action: " + action + " ; Query: " + query);
            // if encoding fails, make sure query contains at least the right command
            // this way a valid request can be made, although it will presumably fail because
            // it's not encoded right.
            return action + "?" + query;
        }
    }

    /***
     * This method is called when the user presses the mainButton
     * @param view The view who called this method
     */
    public void mainButtonPressed(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        if(accountRegistered)
            startActivity(new Intent(this, LobbyActivity.class));// Account registered => start is pressed => Show lobby
        else
            registerAccount(nameBox.getText().toString(), "", null, null);// Account not registered => register is pressed => Register
    }

}
