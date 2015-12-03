package cwa115.trongame;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.FacebookRequest;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;
import cwa115.trongame.Utils.DrawableManager;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_HOME = 0;
    private final static int LOGIN_WELCOME = 1;
    private final static int PROFILE_REQUEST_CODE = 1;

    private CallbackManager callbackManager;
    private AccessToken facebookToken;
    private SharedPreferences settings;

    private HttpConnector dataServer;
    private boolean accountRegistered;

    private ViewFlipper loginViewFlipper;
    private LinearLayout profileControlFooter;
    private Button mainButton;
    private TextView loginWelcomeTextView;
    private ProgressDialog progressDialog;
    private ImageView profilePicture;

    private ConnectivityManager connectivityManager;
    private LocationManager locationManager;

    //region Activity Lifecycle Methods
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
        profileControlFooter = (LinearLayout)findViewById(R.id.profileControlFooter);
        loginWelcomeTextView = (TextView)findViewById(R.id.login_welcome_textview);
        profilePicture = (ImageView)findViewById(R.id.mainActivityProfilePicture);

        // Load managers
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
    }

    @Override
    protected void onPause(){
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        // Update UI
        if(!isInternetEnabled())
            buildAlertMessageNoInternet();// No internet connection
        else {
            // If a user is signed in, show welcome view, otherwise show login view
            Profile localProfile = Profile.Load(settings);
            if (localProfile.getId()!=null && localProfile.getToken()!=null)
                showWelcomeView(true, true);
            else
                showLoginView();
        }
    }
    //endregion

    //region Other Activity Methods
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
        if(requestCode==PROFILE_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                // Do something with returned data
            }
        }else
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    //endregion

    //region UI-control
    private void showWelcomeView(boolean updateUserData, boolean updateFacebookData) {
        // User is already registered
        accountRegistered = true;
        facebookToken = AccessToken.getCurrentAccessToken();

        // Load local stored user settings
        Profile localProfile = Profile.Load(settings);
        // Store userdata in GameSettings
        // Might be overridden if the local data turns out be incorrect
        GameSettings.setProfile(localProfile);

        // Update current userdata if requested
        if(updateUserData && updateFacebookData){
            //progressDialog = ProgressDialog.show(this, "Updating userdata","Please wait...",true,false);
            if(isFacebookUser())
                updateServerUserData(localProfile, true);
            else
                updateServerUserData(localProfile, false);
            Log.d("UPDATE_PROFILE","GetServerData1");
            return;
        }else if(updateFacebookData){
            updateFacebookUserData(localProfile);// updateFacebookUserData will automatically call updateServerUserData
            Log.d("UPDATE_PROFILE","GetFacebookData");
            return;
        }else if(updateUserData){
            updateServerUserData(localProfile, false);
            Log.d("UPDATE_PROFILE","GetServerData2");
            return;
        }

        // Dismiss progressDialog
        //progressDialog.dismiss();

        // Update UI
        loginViewFlipper.setDisplayedChild(LOGIN_WELCOME);// Show welcome screen
        loginWelcomeTextView.setText(String.format(getString(R.string.welcome_message), localProfile.getName()));
        if(localProfile.getPictureUrl()==null)
            profilePicture.setImageResource(R.mipmap.default_profile_picture);
        else
            GameSettings.drawableCache.fetchDrawableAsync(localProfile.getPictureUrl(), profilePicture);

        mainButton.setText(getString(R.string.start));
        mainButton.setTextSize(60);
        profileControlFooter.setVisibility(View.VISIBLE);

        // TODO: check the correctness of the token

        // Store userdata in GameSettings
        GameSettings.drawableCache = new DrawableManager(5);
    }

    private void showLoginView() {
        // User is not registered
        accountRegistered = false;

        // Dismiss progressDialog
        //progressDialog.dismiss();

        // Update UI
        mainButton.setText(getString(R.string.register));
        mainButton.setTextSize(40);
        profileControlFooter.setVisibility(View.GONE);
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
    //endregion

    /***
     * This method loads the facebook data when the user signs in with facebook. This data is used
     * to create an account on our server by calling registerFacebookAccount.
     */
    private void loadFacebookUserData() {
        //progressDialog = ProgressDialog.show(this, "Loading userdata", "Please wait...", true, false);
        FacebookRequest.sendRequest(facebookToken, new FacebookRequest.Callback() {
            @Override
            public void handleResult(Profile facebookProfile) {
                // Then we register the user on our server
                registerFacebookAccount(facebookProfile);
            }
        });
    }

    /***
     * This method gets the latest userdata from facebook and stores the modified data to our server.
     * Once the data is collected, it'll call updateUserData so the modified server data is also
     * stored locally. The method updateUserData will show the welcome view afterwards.
     * @param localProfile The profile of the user whose data will be collected (necessary for the updateUserData call)
     */
    private void updateFacebookUserData(final Profile localProfile) {
        FacebookRequest.sendRequest(facebookToken, new FacebookRequest.Callback() {
            @Override
            public void handleResult(final Profile newProfile) {
                // newProfile contains FACEBOOK-userids instead of SERVER-userids:
                if (newProfile.getFriends().size() > 0) {
                    // Get corresponding friend ids
                    dataServer.sendRequest(
                            ServerCommand.GET_FRIEND_IDS,
                            ImmutableMap.of("facebookIds", new JSONArray(newProfile.getFriends().ToIdList()).toString()),
                            new HttpConnector.Callback() {
                                @Override
                                public void handleResult(String data) {
                                    try {
                                        JSONObject result = new JSONObject(data);
                                        newProfile.setFriends(new FriendList(result.getString("friends")));
                                        Profile dataToUpdate = Profile.GetUpdatedData(localProfile, newProfile);
                                        pushUpdatedDataToServer(localProfile, dataToUpdate);
                                    } catch (JSONException e) {
                                        showToast(R.string.update_failed);
                                    }
                                }
                            });
                } else {
                    Profile dataToUpdate = Profile.GetUpdatedData(localProfile, newProfile);
                    pushUpdatedDataToServer(localProfile, dataToUpdate);
                }
            }
        });
    }

    private void pushUpdatedDataToServer(final Profile localProfile, final Profile dataToUpdate) {
        // The Id and token can't change, so put them in dataToUpdate so a proper query can be created
        dataToUpdate.setId(localProfile.getId());
        dataToUpdate.setToken(localProfile.getToken());
        dataServer.sendRequest(
                ServerCommand.UPDATE_ACCOUNT,
                dataToUpdate.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM, Profile.SERVER_NAME_PARAM, Profile.SERVER_PICTURE_URL_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (result.getBoolean("success")) {
                                if (dataToUpdate.getFriends() != null) {
                                    // Also add new friends:
                                    Map<String, String> query = dataToUpdate.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM, Profile.SERVER_FRIENDS_PARAM);
                                    query.put("accepted", "1");
                                    dataServer.sendRequest(
                                            ServerCommand.ADD_FRIENDS,
                                            query,
                                            new HttpConnector.Callback() {
                                                @Override
                                                public void handleResult(String data) {
                                                    updateServerUserData(localProfile, false);
                                                }
                                            }
                                    );
                                } else
                                    updateServerUserData(localProfile, false);
                            } else
                                showToast(R.string.update_failed);
                        } catch (JSONException e) {
                            showToast(R.string.update_failed);
                        }
                    }
                });
    }

    /***
     * This method downloads the latest userdata from the server and stores it locally. Once the data
     * is collected, it'll show the welcome view to the user.
     * @param localProfile Profile of the user whose data will be downloaded
     */
    private void updateServerUserData(Profile localProfile, final boolean updateFacebookAfterwards){
        dataServer.sendRequest(
                ServerCommand.SHOW_ACCOUNT,
                localProfile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (!result.has("error")) {
                                new Profile(
                                        result.getString("name"),
                                        result.getString("pictureUrl"),
                                        result.getJSONArray("friends")
                                ).Store(settings);
                                showWelcomeView(false, updateFacebookAfterwards);// Update UI
                            } else {
                                // User was not found on server
                                showToast("Profile not found.");
                                // Completely remove the account and settings
                                resetAccountSettings(null);
                            }
                        } catch (JSONException e) {
                            showToast(R.string.update_failed);
                        }
                    }
                });
    }

    /**
     * This method registers a facebook user on our server. First all his friends' userIds are loaded
     * from our server. These friend ids are then sent to the registerAccount method along with the
     * previously loaded facebook data (name & pictureUrl).
     * @param facebookProfile The user's facebook profile, containing FACEBOOK-userIds as Friends.
     *                        These ids are sent to our server. The server converts them to SERVER-
     *                        userIds of the user's friends. These SERVER-userIds are then sent to
     *                        registerAccount along with the other facebook data (loaded by
     *                        loadFacebookUserData) stored in this profile.
     */
    private void registerFacebookAccount(final Profile facebookProfile){
        // First receive userIds of friends based on their facebookIds:
        dataServer.sendRequest(
                ServerCommand.GET_FRIEND_IDS,
                ImmutableMap.of("facebookIds", new JSONArray(facebookProfile.getFriends().ToIdList()).toString()),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            facebookProfile.setFriends(new FriendList(result.getString("friends")));
                            registerAccount(facebookProfile);
                        } catch (JSONException e) {
                            showToast(R.string.register_failed);
                        }
                    }
                });
    }

    /***
     * This method registers an account (local or facebook) on our server. If the account creation
     * is successful, the user's data is stored locally and the welcome view is displayed.
     * @param profile The user's profile data which will be registered on the server.
     */
    private void registerAccount(final Profile profile) {
        if(profile.getName()==null) return;

        dataServer.sendRequest(ServerCommand.INSERT_ACCOUNT, profile.GetQuery(), new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    if(result.has("error"))
                        showToast(R.string.register_failed);
                    else {
                        profile.setId(result.getInt("id"));
                        profile.setToken(result.getString("token"));
                        profile.Store(settings);
                        showToast(R.string.account_created);
                        showWelcomeView(false, false);
                    }
                } catch(JSONException e) {
                    showToast(R.string.register_failed);
                }
            }
        });
    }

    private void deleteAccount(Profile profile) {
        dataServer.sendRequest(
                ServerCommand.DELETE_ACCOUNT,
                profile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            String success = result.getString("success");
                            if (success.equals("true")) {
                                showToast(R.string.account_deleted);
                                showLoginView();
                            } else
                                showToast(R.string.delete_failed);

                        } catch (JSONException e) {
                            showToast(R.string.delete_failed);
                        }
                    }
                });
    }

    //region Button Press Events
    /***
     * This method is called when the user presses the mainButton
     * @param view The view who called this method
     */
    public void mainButtonPressed(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name_entry);
        if(!accountRegistered)
            registerAccount(new Profile(nameBox.getText().toString()));// Account not registered => register is pressed => Register
        else{
            // Check if gps is enabled
            if (!isGpsEnabled())
                buildAlertMessageNoGps();
            else
                startActivity(new Intent(this, LobbyActivity.class));// Account registered => start is pressed => Show lobby
        }
    }

    public void showProfile(View view){
        Bundle data = new Bundle();
        data.putParcelable(ProfileActivity.PROFILE_EXTRA, GameSettings.getProfile());

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.DATA_EXTRA, data);

        startActivityForResult(intent, PROFILE_REQUEST_CODE);
    }

    public void resetAccountSettings(View view) {
        if(!accountRegistered)// Make sure a user is actually signed in
            return;
        accountRegistered = false;// User isn't registered anymore

        if (GameSettings.getProfile() != null) {
            // Delete user's account
            deleteAccount(GameSettings.getProfile());
        }

        if(isFacebookUser())
            LoginManager.getInstance().logOut();// Logout from facebook
        facebookToken = null;// Reset token

        // Delete saved user settings
        Profile.Delete(settings);
        GameSettings.setProfile(null);
        showLoginView();
    }
    //endregion

    //region Helper Methods
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

    // Source from http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
    private boolean isGpsEnabled(){
        return locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }
    private boolean isInternetEnabled(){
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps_disabled_message))
                .setCancelable(false)
                .setPositiveButton(R.string.accept_popup, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.decline_popup, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void buildAlertMessageNoInternet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.internet_unavailable_message))
                .setCancelable(false)
                .setPositiveButton(R.string.accept_popup, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.decline_popup, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    //endregion
}
