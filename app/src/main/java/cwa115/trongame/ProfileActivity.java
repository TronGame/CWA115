package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Lists.LobbyCustomAdapter;
import cwa115.trongame.Lists.LobbyListItem;
import cwa115.trongame.Lists.StatsCustomAdapter;
import cwa115.trongame.Lists.StatsListItem;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;
import cwa115.trongame.Utils.DrawableManager;

public class ProfileActivity extends AppCompatActivity {

    public final static String DATA_EXTRA = "profileActivity_dataExtra";
    public final static String PROFILE_EXTRA = "profileActivity_profileExtra";

    private final static int FRIEND_LIST_REQUEST_CODE = 2;

    private HttpConnector dataServer;
    private Profile profile;
    private List<StatsListItem> statsList;
    private StatsCustomAdapter statsCustomAdapter;

    private ImageView profileImageView, facebookFlag;
    private TextView usernameTextView;
    private LinearLayout friendFooter;
    private Button friendListButton;
    private ListView statsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Load profile to display
        Intent i = getIntent();
        Bundle data = i.getBundleExtra(DATA_EXTRA);
        //data.setClassLoader(Profile.class.getClassLoader());
        profile = data.getParcelable(PROFILE_EXTRA);

        // Dataserver reference
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        // Get references to UI elements
        profileImageView = (ImageView)findViewById(R.id.profileImageView);
        facebookFlag = (ImageView)findViewById(R.id.facebookFlag);
        usernameTextView = (TextView)findViewById(R.id.userNameTextView);
        friendFooter = (LinearLayout)findViewById(R.id.friendFooter);
        friendListButton = (Button)findViewById(R.id.friendListButton);
        statsListView = (ListView)findViewById(R.id.statsListView);

        loadProfile();
    }

    @Override
    protected void onResume(){
        super.onResume();
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
        if(profile.getName()==null){
            finish();
            return;
        }else
            usernameTextView.setText(profile.getName());

        if(profile.getId()!=null && profile.getToken()!=null) { // Assume a valid id and token is set
            friendFooter.setVisibility(View.GONE);// So user views his own profile
            friendListButton.setVisibility(View.VISIBLE);
        }else {
            friendFooter.setVisibility(View.VISIBLE);
            friendListButton.setVisibility(View.GONE);
        }
        if(profile.getPictureUrl()==null)
            profileImageView.setImageResource(R.mipmap.default_profile_picture);
        else
            DrawableManager.cache.fetchDrawableAsync(profile.getPictureUrl(), profileImageView);

        if(profile.getFacebookId()==null)
            facebookFlag.setVisibility(View.GONE);
        else
            facebookFlag.setVisibility(View.VISIBLE);

        loadStats();
    }

    private void loadStats(){
        statsList = new ArrayList<>();
        statsList.add(new StatsListItem("Global Stats"));
        statsList.add(new StatsListItem("Total wins","5 (33%)"));
        statsList.add(new StatsListItem("Total losses","10 (66%)"));
        statsList.add(new StatsListItem("Highscore","12345"));
        statsList.add(new StatsListItem("Total play time","7 hours 18 minutes"));
        statsList.add(new StatsListItem("Achievements"));
        statsList.add(new StatsListItem("To be implemented",""));
        statsList.add(new StatsListItem("Social Stats"));
        statsList.add(new StatsListItem("Most popular friend","badass biker"));
        statsList.add(new StatsListItem("Number of friends","0"));
        statsList.add(new StatsListItem("Last added friend","God"));
        statsList.add(new StatsListItem("Important note: this is a demo stats list!"));

        statsCustomAdapter = new StatsCustomAdapter(this, statsList);
        statsListView.setAdapter(statsCustomAdapter);
        statsCustomAdapter.notifyDataSetChanged();
    }

    public void showFriendList(View v){
        Bundle data = new Bundle();
        data.putString(FriendsListActivity.TITLE_EXTRA, "Friend List");
        data.putParcelable(FriendsListActivity.PROFILE_EXTRA, profile);
        data.putBoolean(FriendsListActivity.SELECTABLE_EXTRA, true);

        Intent intent = new Intent(this, FriendsListActivity.class);
        intent.putExtra(FriendsListActivity.DATA_EXTRA, data);

        startActivityForResult(intent, FRIEND_LIST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FRIEND_LIST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int[] selectedIds = data.getIntArrayExtra(FriendsListActivity.SELECTED_IDS_EXTRA);
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(
                getBaseContext(), text,
                Toast.LENGTH_SHORT
        ).show();
    }
}
