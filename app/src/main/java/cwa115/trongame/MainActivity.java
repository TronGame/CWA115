package cwa115.trongame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.HttpConnector;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_HOME = 0;
    private final static int LOGIN_WELCOME = 1;
    private final static int LOGIN_REGISTER = 2;

    private final static String ACCOUNT_NAME_KEY = "accountName";
    private final static String ACCOUNT_ID_KEY = "accountId";
    private final static String ACCOUNT_TOKEN_KEY = "accountToken";

    private CallbackManager callbackManager;
    private AccessToken token;
    private SharedPreferences settings;

    private HttpConnector dataServer;
    private boolean accountRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());

        // activity_main contains facebook login button
        setContentView(R.layout.activity_main);

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

    private void showLoginOptions() {
        if(settings.contains(ACCOUNT_NAME_KEY) && settings.contains(ACCOUNT_TOKEN_KEY)) {
            // User is already registered
            accountRegistered = true;
            greetUser();
        } else {
            Button button = (Button) findViewById(R.id.start_button);
            button.setText(getString(R.string.register));
            button.setTextSize(40);
            // Make sure the edit text and facebook button are enabled
            findViewById(R.id.name_entry).setEnabled(true);
            findViewById(R.id.facebook_login_button).setEnabled(true);

            // Allow the user to create an account
            accountRegistered = false;
            showFacebookLogin();
        }
    }

    private void greetUser() {
        String accountName = settings.getString(ACCOUNT_NAME_KEY, null);
        int accountId = settings.getInt(ACCOUNT_ID_KEY, 0);
        String accountToken = settings.getString(ACCOUNT_TOKEN_KEY, null);
        // TODO: check the correctness of the token

        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        nameBox.setText(accountName);
        nameBox.setEnabled(false);
        GameSettings.setPlayerName(accountName);
        GameSettings.setUserId(accountId);
        GameSettings.setPlayerToken(accountToken);

        // Disable Facebook login button
        findViewById(R.id.facebook_login_button).setEnabled(false);
    }

    public void resetAccountSettings(View view) {
        if(!accountRegistered)
            return;
        accountRegistered = false;

        deleteAccount(GameSettings.getUserId(), GameSettings.getPlayerToken());

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ACCOUNT_NAME_KEY);
        editor.remove(ACCOUNT_ID_KEY);
        editor.remove(ACCOUNT_TOKEN_KEY);
        editor.commit();
        showLoginOptions();
    }

    private void showFacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        final ViewFlipper loginViewFlipper = (ViewFlipper) findViewById(R.id.login_view_flipper);

        token = AccessToken.getCurrentAccessToken();
        if(token == null) { // No facebook user is signed in
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    token = loginResult.getAccessToken();
                    Log.d("FACEBOOK_LOGIN", "Login succeeded: " + token.getUserId());
                    // Load user data:
                    loadFacebookUserData();
                }

                @Override
                public void onCancel() {
                    Log.d("FACEBOOK_LOGIN", "Login canceled");
                    Toast.makeText(getBaseContext(),"Login canceled",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("FACEBOOK_LOGIN", "Login error: " + error.toString());
                    Toast.makeText(getBaseContext(),"Login error",Toast.LENGTH_SHORT).show();
                }
            });
        } else {// A facebook user is signed in
            loginViewFlipper.setDisplayedChild(LOGIN_HOME);
            // TODO: Get profile data (saved on our server) and display welcome message
            updateFacebookUserData();
        }
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
        final TextView welcomeTextView = (TextView)findViewById(R.id.login_welcome_textview);
        final ViewFlipper loginViewFlipper = (ViewFlipper)findViewById(R.id.login_view_flipper);
        /* make the API call */
        new GraphRequest(
                token,
                "/" + token.getUserId() + "?fields=name,picture,friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject userData = response.getJSONObject();
                            String name = userData.getString("name");
                            String profilePictureUrl = userData.getString("picture");
                            Object friends = userData.get("friends");
                            Log.d("FACEBOOK_LOGIN", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl);
                            welcomeTextView.setText("Welcome back " + name + "!");
                            loginViewFlipper.setDisplayedChild(LOGIN_HOME);
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
        new GraphRequest(
                token,
                "/" + token.getUserId() + "?fields=name,picture,friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject userData = response.getJSONObject();
                            String name = userData.getString("name");
                            String profilePictureUrl = userData.getString("picture");
                            Object friends = userData.get("friends");
                            Log.d("FACEBOOK_LOGIN", "Userdata available! Name: " + name + " ; profilePicUrl: " + profilePictureUrl);
                            //TODO: push modified data to server
                        }catch(JSONException e){
                            Log.e("FACEBOOK_LOGIN", "An exception occurred while trying to retrieve the user's data.");
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void registerAccount(final String name) {
        final String query = "insertAccount?name=" + name;
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
                    greetUser();
                } catch (JSONException e) {
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
            registerAccount(nameBox.getText().toString());
    }

}
