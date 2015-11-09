package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cwa115.trongame.Test.SensorData;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

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

        AccessToken token = AccessToken.getCurrentAccessToken();
        if(token!=null)
            ((TextView) findViewById(R.id.facebook_token)).setText(token.getUserId());

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FACEBOOK_LOGIN","Login succeeded: " + loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK_LOGIN","Login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FACEBOOK_LOGIN","Login error: " + error.toString());
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
