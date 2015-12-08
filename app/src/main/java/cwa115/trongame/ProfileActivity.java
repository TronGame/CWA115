package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.StatsCustomAdapter;
import cwa115.trongame.Lists.StatsListItem;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.User.Friend;
import cwa115.trongame.User.Profile;
import cwa115.trongame.Utils.DrawableManager;

public class ProfileActivity extends AppCompatActivity {

    public final static String DATA_EXTRA = "profileActivity_dataExtra";
    public final static String PROFILE_EXTRA = "profileActivity_profileExtra";
    public final static String DELETE_ACCOUNT_EXTRA = "profileActivity_deleteAccountExtra";

    private final static int VIEW_PENDING_FRIEND = 0;
    private final static int VIEW_FRIEND_OR_STRANGER = 1;
    private final static int VIEW_OWN_PROFILE = 2;
    private final static int OWN_PROFILE_STATE = 0;
    private final static int STRANGER_PROFILE_STATE = 1;
    private final static int FRIEND_PROFILE_STATE = 2;
    private final static int PENDING_FRIEND_INVITER_STATE = 3;
    private final static int PENDING_FRIEND_INVITEE_STATE = 4;

    private HttpConnector dataServer;
    private Profile profile;
    private List<StatsListItem> statsList;
    private StatsCustomAdapter statsCustomAdapter;
    private int currentState;

    private ImageView profileImageView, facebookFlag;
    private TextView usernameTextView;
    private ViewFlipper footerFlipper;
    private ListView statsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Load profile to display
        Intent i = getIntent();
        Bundle data = i.getBundleExtra(DATA_EXTRA);
        profile = data.getParcelable(PROFILE_EXTRA);

        // Dataserver reference
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        // Get references to UI elements
        profileImageView = (ImageView)findViewById(R.id.profileImageView);
        facebookFlag = (ImageView)findViewById(R.id.facebookFlag);
        usernameTextView = (TextView)findViewById(R.id.userNameTextView);
        footerFlipper = (ViewFlipper)findViewById(R.id.footer_flipper);
        statsListView = (ListView)findViewById(R.id.statsListView);
    }

    @Override
    protected void onResume(){
        super.onResume();

        footerFlipper.setVisibility(View.GONE);
        Profile.Load(
                dataServer,
                profile,
                new Profile.LoadCallback() {
                    @Override
                    public void onProfileLoaded(Profile updatedProfile) {
                        profile = updatedProfile;
                        loadProfile();
                    }

                    @Override
                    public void onProfileNotFound(int id, String token) {
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        showToast("Could not load last profile data.");
                        loadProfile();// Try to load profile with old data
                    }
                });
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Do stuff
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void loadProfile(){
        if(profile.getName()==null || profile.getId()==null){
            finish();
            return;
        }else
            usernameTextView.setText(profile.getName());

        if(profile.getId()==GameSettings.getUserId())
            currentState = OWN_PROFILE_STATE;// User views his own profile
        else if(GameSettings.getFriends()!=null && GameSettings.getFriends().ToIdList().contains((long)profile.getId())){
            Friend friend = GameSettings.getFriends().get(GameSettings.getFriends().ToIdList().indexOf((long)profile.getId()));
            if(friend.isPending()){
                if(!friend.isInviter()){
                    currentState = PENDING_FRIEND_INVITEE_STATE;// Current user is inviter
                }else{
                    currentState = PENDING_FRIEND_INVITER_STATE;// Current user is invited
                }
            }else
                currentState = FRIEND_PROFILE_STATE;// Player is a friend
        }else
            currentState = STRANGER_PROFILE_STATE;// Player is not a friend

        updateFooterFlipper();

        if(profile.getPictureUrl()==null)
            profileImageView.setImageResource(R.mipmap.default_profile_picture);
        else
            DrawableManager.cache.fetchDrawableAsync(profile.getPictureUrl(), profileImageView);

        if(profile.getFacebookId()==null)
            facebookFlag.setVisibility(View.GONE);
        else
            facebookFlag.setVisibility(View.VISIBLE);

        if(profile.getFriends()!=null && profile.getFriends().size()>0){
            // Get last added friend name and most popular friend name:
            Friend firstFriend = profile.getFriends().get(0);
            int mostCommonPlays = firstFriend.getCommonPlays();
            long mostCommonPlaysFriendId = firstFriend.getId();
            for(Friend friend : profile.getFriends()){
                if(friend.getCommonPlays()>mostCommonPlays)
                    mostCommonPlaysFriendId = friend.getId();
            }
            // First friend is last added friend
            getFriendNamesAndLoadStats(firstFriend.getId(), mostCommonPlaysFriendId);
        }else
            loadStats("/", "/");// Load stats without friend names
    }

    private void updateFooterFlipper(){
        switch(currentState){
            case OWN_PROFILE_STATE:
                footerFlipper.setDisplayedChild(VIEW_OWN_PROFILE);
                break;
            case STRANGER_PROFILE_STATE:
                footerFlipper.setDisplayedChild(VIEW_FRIEND_OR_STRANGER);
                ((TextView) findViewById(R.id.friend_text)).setText(R.string.not_friends_message);
                ((Button) findViewById(R.id.friend_button)).setText(R.string.add_friend);
                break;
            case FRIEND_PROFILE_STATE:
                footerFlipper.setDisplayedChild(VIEW_FRIEND_OR_STRANGER);
                ((TextView) findViewById(R.id.friend_text)).setText(R.string.friends_message);
                ((Button) findViewById(R.id.friend_button)).setText(R.string.delete_friend);
                break;
            case PENDING_FRIEND_INVITEE_STATE:
                // Current user is inviter, so he can only reject the friend request
                findViewById(R.id.accept_friend_request).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.friend_pending_text)).setText(R.string.friends_pending_invitee_message);
                footerFlipper.setDisplayedChild(VIEW_PENDING_FRIEND);// Player is a pending friend
                break;
            case PENDING_FRIEND_INVITER_STATE:
                // Current user is invited
                ((TextView)findViewById(R.id.friend_pending_text)).setText(R.string.friends_pending_inviter_message);
                footerFlipper.setDisplayedChild(VIEW_PENDING_FRIEND);// Player is a pending friend
                break;
        }
        footerFlipper.setVisibility(View.VISIBLE);
    }

    private void getFriendNamesAndLoadStats(long lastAddedFriendId, final long mostPopularFriendId){
        Profile.Load(
                dataServer,
                (int) lastAddedFriendId,
                null,
                new Profile.LoadCallback() {
                    @Override
                    public void onProfileLoaded(Profile profile) {
                        final String lastAddedFriendName = profile.getName() == null ? "/" : profile.getName();

                        Profile.Load(
                                dataServer,
                                (int) mostPopularFriendId,
                                null,
                                new Profile.LoadCallback() {
                                    @Override
                                    public void onProfileLoaded(Profile profile) {
                                        String mostPopularFriendName = profile.getName() == null ? "/" : profile.getName();
                                        loadStats(lastAddedFriendName, mostPopularFriendName);
                                    }

                                    @Override
                                    public void onProfileNotFound(int id, String token) {
                                        showToast("Most popular friend's profile not found");
                                        loadStats(lastAddedFriendName, "/");
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        showToast("Error while trying to get most popular friend's name");
                                        loadStats(lastAddedFriendName, "/");
                                    }
                                }
                        );
                    }

                    @Override
                    public void onProfileNotFound(int id, String token) {
                        showToast("Last added friend's profile not found");
                        loadStats("/", "/");
                    }

                    @Override
                    public void onError(Exception e) {
                        showToast("Error while trying to get last added friend's name");
                        loadStats("/", "/");
                    }
                }
        );
    }

    private void loadStats(String lastAddedFriend, String mostPopularFriend){
        int wins = profile.getWins();
        int losses = profile.getLosses();
        double totalPlays = wins+losses;
        String winRatio = totalPlays==0 ? "" : " (" + Math.round((double)wins/totalPlays*100) + "%)";
        String lossRatio = totalPlays==0 ? "" : " (" + Math.round((double)losses/totalPlays*100) + "%)";
        int numberFriends = (profile.getFriends()==null) ? 0 : profile.getFriends().size();
        statsList = new ArrayList<>();
        statsList.add(new StatsListItem("Global Stats"));
        statsList.add(new StatsListItem("Total wins",wins + winRatio));
        statsList.add(new StatsListItem("Total losses",losses + lossRatio));
        statsList.add(new StatsListItem("Highscore",String.valueOf(profile.getHighscore())));
        if(profile.getToken()!=null) { // Only if token is set, we have access to these stats
            statsList.add(new StatsListItem("Total play time", formatPlaytime(profile.getPlaytime())));
            statsList.add(new StatsListItem("Social Stats"));
            statsList.add(new StatsListItem("Most popular friend", mostPopularFriend));
            statsList.add(new StatsListItem("Number of friends", String.valueOf(numberFriends)));
            statsList.add(new StatsListItem("Last added friend", lastAddedFriend));// TODO: sort friends so last added one is the first in the list
        }
        statsList.add(new StatsListItem("Achievements"));
        statsList.add(new StatsListItem("To be implemented",""));

        statsCustomAdapter = new StatsCustomAdapter(this, statsList);
        statsListView.setAdapter(statsCustomAdapter);
        statsCustomAdapter.notifyDataSetChanged();
    }

    private String formatPlaytime(int playtime){
        int days = playtime / (24*60*60);
        int rest = playtime % (24*60*60);
        int hours = rest / (60*60);
        rest %= 60*60;
        int minutes = rest / 60;
        if(days!=0)
            return String.format(getString(R.string.days_hours_minutes), days, hours, minutes);
        else if(hours!=0)
            return String.format(getString(R.string.hours_minutes), hours, minutes);
        else
            return String.format(getString(R.string.minutes), minutes);
    }

    public void showFriendList(View v){
        Bundle data = new Bundle();
        data.putString(FriendsListActivity.TITLE_EXTRA, "Friend List");
        data.putParcelable(FriendsListActivity.PROFILE_EXTRA, profile);

        Intent intent = new Intent(this, FriendsListActivity.class);
        intent.putExtra(FriendsListActivity.DATA_EXTRA, data);

        startActivity(intent);
    }

    public void deleteAccount(View v){
        Intent data = new Intent();
        data.putExtra(DELETE_ACCOUNT_EXTRA, true);
        setResult(RESULT_OK, data);
        finish();
    }

    public void acceptFriendRequest(View v) {
        dataServer.sendRequest(
                ServerCommand.ACCEPT_FRIEND,
                ImmutableMap.of(
                        "id", GameSettings.getPlayerId(),
                        "token", GameSettings.getPlayerToken(),
                        "friendId", String.valueOf(profile.getId())
                ),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (!result.has("error") && result.getBoolean("success")) {
                                showToast("Friend request accepted!");
                                currentState = FRIEND_PROFILE_STATE;
                                updateFooterFlipper();
                            } else
                                showToast("Error while trying to accept friend request.");
                        } catch (JSONException e) {
                            showToast("Error while accepting friend request.");
                        }
                    }
                }
        );
    }

    public void rejectFriendRequest(View v){
        dataServer.sendRequest(
                ServerCommand.DELETE_FRIEND,
                ImmutableMap.of(
                        "id", GameSettings.getPlayerId(),
                        "token", GameSettings.getPlayerToken(),
                        "friendId", String.valueOf(profile.getId())
                ),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try{
                            JSONObject result = new JSONObject(data);
                            if(!result.has("error") && result.getBoolean("success")) {
                                showToast("Friend request rejected!");
                                currentState = STRANGER_PROFILE_STATE;
                                updateFooterFlipper();
                            }else
                                showToast("Error while trying to reject friend request.");
                        }catch (JSONException e){
                            showToast("Error while rejecting friend request.");
                        }
                    }
                }
        );
    }

    public void friendButtonPressed(View v){
        if(currentState==STRANGER_PROFILE_STATE){
            // Add as friend
            dataServer.sendRequest(
                    ServerCommand.ADD_FRIENDS,
                    ImmutableMap.of(
                            "id", GameSettings.getPlayerId(),
                            "token", GameSettings.getPlayerToken(),
                            "friends", "[" + String.valueOf(profile.getId()) + "]"
                    ),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {
                            try {
                                JSONObject result = new JSONObject(data);
                                if (!result.has("error") && result.getBoolean("success")) {
                                    showToast("Friend request sent!");
                                    currentState = PENDING_FRIEND_INVITEE_STATE;
                                    updateFooterFlipper();
                                } else
                                    showToast("Error while trying to send friend request.");
                            } catch (JSONException e) {
                                showToast("Error while sending friend request.");
                            }
                        }
                    }
            );
        }else if(currentState==FRIEND_PROFILE_STATE){
            // Delete friend
            dataServer.sendRequest(
                    ServerCommand.DELETE_FRIEND,
                    ImmutableMap.of(
                            "id", GameSettings.getPlayerId(),
                            "token", GameSettings.getPlayerToken(),
                            "friendId", String.valueOf(profile.getId())
                    ),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {
                            try{
                                JSONObject result = new JSONObject(data);
                                if(!result.has("error") && result.getBoolean("success")) {
                                    showToast("Friend deleted!");
                                    currentState = STRANGER_PROFILE_STATE;
                                    updateFooterFlipper();
                                }else
                                    showToast("Error while trying to delete friend.");
                            }catch (JSONException e){
                                showToast("Error while deleting friend.");
                            }
                        }
                    }
            );
        }
    }

    private void showToast(String text) {
        Toast.makeText(
                getBaseContext(), text,
                Toast.LENGTH_SHORT
        ).show();
    }
}
