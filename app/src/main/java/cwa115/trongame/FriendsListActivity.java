package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;

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

public class FriendsListActivity extends AppCompatActivity {

    public final static String DATA_EXTRA = "friendsListActivity_dataExtra";
    public final static String TITLE_EXTRA = "friendsListActivity_titleExtra";
    public final static String SELECTABLE_EXTRA = "friendsListActivity_selectableExtra";
    public final static String PROFILE_EXTRA = "friendsListActivity_profileExtra";
    public final static String SELECTED_IDS_EXTRA = "friendsListActivity_selectedIdsExtra";
    public final static String FROM_NOTIFICATION_EXTRA = "friendsListActivity_fromNotificationExtra";

    private HttpConnector dataServer;
    private boolean selectable;
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
        profile = data.getParcelable(PROFILE_EXTRA);

        // Initialize server connection
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        // Get reference to UI elements
        mainList = (ListView) findViewById(R.id.mainList);
    }


    @Override
    protected void onResume(){
        super.onResume();

        if(profile.getFriends()!=null)
            buildFriendsList();
        else {
            // Make server request to get friends:
            dataServer.sendRequest(
                    ServerCommand.SHOW_ACCOUNT,
                    profile.GetQuery(Profile.SERVER_ID_PARAM),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {
                            try {
                                JSONObject result = new JSONObject(data);
                                profile.setFriends(new FriendList(result.getJSONArray("friends")));
                                buildFriendsList();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showToast("Couldn't get friend data from server.");
                                cancel();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancel();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void buildFriendsList(){
        friendListItems = new ArrayList<>();
        final FriendList friends = profile.getFriends();

        for (final Friend friend : friends) {
            // Get name of each friend
            dataServer.sendRequest(
                    ServerCommand.SHOW_ACCOUNT,
                    ImmutableMap.of("id", String.valueOf(friend.getId())),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {
                            try {
                                JSONObject result = new JSONObject(data);
                                friendListItems.add(new FriendListItem(friend.getId(), result.getString("name")));
                                if (friendListItems.size() == friends.size()) // All friend names are received
                                    populateFriendsList();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showToast("JSONError while trying to get friend name.");
                            }
                        }
                    }
            );
        }
    }

    private void populateFriendsList(){
        adapter = new FriendListAdapter(this, friendListItems, selectable);
        mainList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void commitButtonPressed(View v){
        List<Long> selectedIds = new ArrayList<>();
        for(FriendListItem item : friendListItems){
            if(item.isSelected())
                selectedIds.add(item.getPlayerId());
        }
        commit(Longs.toArray(selectedIds));
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
    private void commit(long[] selectedIds){
        Intent data = new Intent();
        data.putExtra(SELECTED_IDS_EXTRA, selectedIds);
        setResult(RESULT_OK, data);
        finish();
    }
}
