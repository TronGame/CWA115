package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.FriendListAdapter;
import cwa115.trongame.Lists.FriendListItem;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.User.Friend;
import cwa115.trongame.User.FriendList;
import cwa115.trongame.User.Profile;
import cwa115.trongame.Utils.DrawableManager;

public class FriendsListActivity extends AppCompatActivity implements FriendListAdapter.Callback {

    public final static String DATA_EXTRA = "friendsListActivity_dataExtra";
    public final static String TITLE_EXTRA = "friendsListActivity_titleExtra";
    public final static String COMMIT_TEXT_EXTRA = "friendsListActivity_commitTextExtra";
    public final static String SELECTABLE_EXTRA = "friendsListActivity_selectableExtra";
    public final static String PROFILE_EXTRA = "friendsListActivity_profileExtra";
    public final static String SELECTED_IDS_EXTRA = "friendsListActivity_selectedIdsExtra";
    public final static String FROM_NOTIFICATION_EXTRA = "friendsListActivity_fromNotificationExtra";

    private HttpConnector dataServer;
    private boolean selectable, shouldExitSafely;
    private Profile profile;
    private List<FriendListItem> friendListItems;
    private FriendListAdapter adapter;

    private ListView mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendslist);

        // Read extra data:
        Intent intent = getIntent();
        Bundle data = intent.getBundleExtra(DATA_EXTRA);
        if(!data.containsKey(TITLE_EXTRA) || !data.containsKey(PROFILE_EXTRA))
            cancel();

        // If we came here from a notification, make sure some basic initializations (which are
        // normally performed in MainActivity or ProfileActivity) are performed here:
        if(data.containsKey(FROM_NOTIFICATION_EXTRA) && data.getBoolean(FROM_NOTIFICATION_EXTRA)){
            GameSettings.setProfile(Profile.Load(PreferenceManager.getDefaultSharedPreferences(this)));
            DrawableManager.InitializeCache(5);
        }

        ((TextView)findViewById(R.id.friends_list_title)).setText(data.getString(TITLE_EXTRA));

        selectable = false;
        if(data.containsKey(SELECTABLE_EXTRA))
            selectable = data.getBoolean(SELECTABLE_EXTRA);
        else
            findViewById(R.id.friends_list_commitButton).setVisibility(View.GONE);

        if(data.containsKey(COMMIT_TEXT_EXTRA))
            ((Button)findViewById(R.id.friends_list_commitButton)).setText(data.getString(COMMIT_TEXT_EXTRA));
        profile = data.getParcelable(PROFILE_EXTRA);
        if(profile==null || profile.getId()==null || profile.getToken()==null)
            finish();

        shouldExitSafely = data.getBoolean(RoomActivity.FROM_ROOMACTIVITY_EXTRA, false);

        // Initialize server connection
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        // Get reference to UI elements
        mainList = (ListView) findViewById(R.id.mainList);
    }


    @Override
    protected void onResume(){
        super.onResume();

        Profile.Load(
                dataServer,
                profile,
                new Profile.LoadCallback() {
                    @Override
                    public void onProfileLoaded(Profile updatedProfile) {
                        profile = updatedProfile;
                        if (profile.getFriends() != null)
                            buildFriendsList();
                        // TODO: show message that no friends are available
                    }

                    @Override
                    public void onProfileNotFound(int id, String token) {
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        showToast("Could not load last friends data.");
                        if (profile.getFriends() != null)
                            buildFriendsList();// Try to load friends with old data
                    }
                });
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(shouldExitSafely){
            // We are leaving the entire application (probably user pressed home button) OR GameActivity is launched:
            //RoomActivity.StaticSafeExit(dataServer);
            RoomActivity.roomUpdater.cancel();// Cancel updater before proceeding
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // We are going back without leaving the entire application, so RoomActivity will handle the safeExit
            shouldExitSafely = false;
            cancel();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void buildFriendsList(){
        friendListItems = new ArrayList<>();
        final FriendList friends = profile.getFriends();

        for (final Friend friend : friends) {
            // Get profile of each friend
            Profile.Load(
                    dataServer,
                    (int) friend.getId(),
                    null,
                    new Profile.LoadCallback() {
                        @Override
                        public void onProfileLoaded(Profile friendProfile) {
                            friendListItems.add(new FriendListItem(friend, friendProfile));
                            if (friendListItems.size() == friends.size()) // All friend names are received
                                populateFriendsList();
                        }

                        @Override
                        public void onProfileNotFound(int id, String token) {
                            showToast("Friend's profile not found.");
                        }

                        @Override
                        public void onError(Exception e) {
                            showToast("Error while trying to get friend's data.");
                        }
                    }
            );
        }
    }

    private void populateFriendsList(){
        // Store the scroll position
        View view = mainList.getChildAt(0);
        int indexPosition = mainList.getFirstVisiblePosition();
        int top = (view == null) ? 0 : (view.getTop() - mainList.getPaddingTop());

        adapter = new FriendListAdapter(this, friendListItems, selectable, this);
        mainList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(!selectable){
            mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Profile friendProfile = friendListItems.get(position).getPlayerProfile();

                    Bundle data = new Bundle();
                    data.putParcelable(ProfileActivity.PROFILE_EXTRA, friendProfile);

                    Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                    intent.putExtra(ProfileActivity.DATA_EXTRA, data);

                    startActivity(intent);
                }
            });
        }

        mainList.setSelectionFromTop(indexPosition, top);
    }

    public void commitButtonPressed(View v){
        ArrayList<Integer> selectedIds = new ArrayList<>();
        for(FriendListItem item : friendListItems){
            if(item.isSelected())
                selectedIds.add((int)item.getPlayer().getId());
        }
        commit(selectedIds);
    }

    private void showToast(String text) {
        Toast.makeText(
                getBaseContext(), text,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void cancel(){
        setResult(RESULT_CANCELED);
        finish();
    }
    private void commit(ArrayList<Integer> selectedIds){
        Intent data = new Intent();
        data.putExtra(SELECTED_IDS_EXTRA, selectedIds);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onFriendAccepted(Friend friend, final int friendPosition) {
        dataServer.sendRequest(
                ServerCommand.ACCEPT_FRIEND,
                ImmutableMap.of(
                        "id", String.valueOf(profile.getId()),
                        "token", profile.getToken(),
                        "friendId", String.valueOf(friend.getId())),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try{
                            JSONObject result = new JSONObject(data);
                            if(!result.has("error") && result.getBoolean("success")){
                                showToast("Friend request accepted.");
                                friendListItems.get(friendPosition).getPlayer().accept();
                                adapter.notifyDataSetChanged();
                            }else
                                showToast("Error while accepting friend request.");
                        }catch(JSONException e){
                            showToast("Error while accepting friend request.");
                        }
                    }
                }
        );
    }

    @Override
    public void onFriendRejected(Friend friend, final int friendPosition) {
        dataServer.sendRequest(
                ServerCommand.DELETE_FRIEND,
                ImmutableMap.of(
                        "id", String.valueOf(profile.getId()),
                        "token", profile.getToken(),
                        "friendId", String.valueOf(friend.getId())
                ),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try{
                            JSONObject result = new JSONObject(data);
                            if(!result.has("error") && result.getBoolean("success")){
                                showToast("Friend request rejected.");
                                friendListItems.remove(friendPosition);
                                adapter.notifyDataSetChanged();
                            }else
                                showToast("Error while rejecting friend request.");
                        }catch(JSONException e){
                            showToast("Error while rejecting friend request.");
                        }
                    }
                }
        );
    }
}
