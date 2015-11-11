package cwa115.trongame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import cwa115.trongame.Game.GameSettings;

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

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_HOME = 0;
    private final static int LOGIN_WELCOME = 1;
    private final static int LOGIN_REGISTER = 2;

    private CallbackManager callbackManager;
    private AccessToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.

        setContentView(R.layout.activity_main);// activity_main contains facebook login button

        GameSettings.setPlayerMarkerImage(R.mipmap.markerk);
        // Initialize SensorData
        //SensorData.Initialize(this);
        //SensorData.Test(this); // Only for testing purposes

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        final ViewFlipper loginViewFlipper = (ViewFlipper) findViewById(R.id.login_view_flipper);

        token = AccessToken.getCurrentAccessToken();
        if(token==null) {// No facebook user is signed in
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
        }else{// A facebook user is signed in
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

    public void showGameActivity(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        GameSettings.setPlayerName(nameBox.getText().toString());
        startActivity(new Intent(this, GameActivity.class));
    }
    public void showLobbyActivity(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        GameSettings.setPlayerName(nameBox.getText().toString());
        startActivity(new Intent(this, LobbyActivity.class));
    }

}
