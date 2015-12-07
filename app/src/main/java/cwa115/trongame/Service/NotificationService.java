package cwa115.trongame.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.User.Friend;
import cwa115.trongame.User.FriendList;
import cwa115.trongame.FriendsListActivity;
import cwa115.trongame.LobbyActivity;
import cwa115.trongame.MainActivity;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.User.Profile;
import cwa115.trongame.ProfileActivity;
import cwa115.trongame.R;
import cwa115.trongame.RoomActivity;

/**
 * Code inspiration from http://it-ride.blogspot.be/2010/10/android-implementing-notification.html
 */
public class NotificationService extends Service {

    private final static String TAG = "NotificationService";
    private final static int FRIEND_NOTIFICATION_ID = 1000000;
    private final static int GAME_NOTIFICATION_ID = 2000000;

    private PowerManager.WakeLock mWakeLock;
    private HttpConnector dataServer;
    private boolean gameInvitesReceived, friendInvitesReceived;
    private Profile profile;
    private int totalGameInvites, totalFriendInvites, handledGameInvites, handledFriendInvites;

    /**
     * Simply return null, since our Service will not be communicating with
     * any other components. It just does its work silently.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This is where we initialize. We call this when onStart/onStartCommand is
     * called by the system. We won't do anything with the intent here, and you
     * probably won't, either.
     */
    private void handleIntent(Intent intent) {
        // obtain the wake lock
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Log.d(TAG, "No internet connection => Stop service");
            stopSelf();
            return;
        }

        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        profile = Profile.Load(settings);
        friendInvitesReceived = false;
        gameInvitesReceived = false;
        handledGameInvites = 0;
        handledFriendInvites = 0;
        if(profile.getId()==null || profile.getToken()==null) { // The user isn't signed in
            Log.d(TAG, "No user signed in => Stop service");
            stopSelf();
            return;
        }
        // do the actual work, in a separate thread
        Log.d(TAG, "Send SHOW_INVITES server request.");
        dataServer.sendRequest(
                ServerCommand.SHOW_INVITES,
                profile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        handleShowInvites(data);
                    }
                }
        );
        Log.d(TAG, "Send SHOW_ACCOUNT server request.");
        dataServer.sendRequest(
                ServerCommand.SHOW_ACCOUNT,
                profile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        handleShowAccount(data);
                    }
                });
    }

    private void handleShowInvites(String data){
        try{
            Log.d(TAG, "SHOW_INVITES result");
            JSONObject result = new JSONObject(data);
            if(!result.has("error") && result.optBoolean("invites",true)) {// If token is wrong (which may happen when account is deleted), invites will be false
                JSONArray invites = result.getJSONArray("invites");
                totalGameInvites = invites.length();
                if(totalGameInvites==0) gameInvitesFinished();// Make sure gameInvitesFinished is called even when there are no invites
                for (int i = 0; i < totalGameInvites; i++) {
                    JSONObject invite = new JSONObject(invites.getString(i));
                    final int inviteId = invite.getInt("inviteId");
                    final int inviterId = invite.getInt("inviterId");
                    final int gameId = invite.getInt("gameId");
                    dataServer.sendRequest(
                            ServerCommand.SHOW_GAME,
                            ImmutableMap.of("gameId", String.valueOf(gameId)),
                            new HttpConnector.Callback() {
                                @Override
                                public void handleResult(String data) {
                                    handleShowGame(data, inviteId, inviterId, gameId);
                                }
                            }
                    );
                }
            }else {
                Log.e(TAG, "Server error while receiving gameInvites.");
                gameInvitesFinished();
            }
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(TAG, "JSON error while receiving gameInvites.");
            gameInvitesFinished();
        }
    }

    private void handleShowGame(String data, final int inviteId, final int inviterId, final int gameId){
        try {
            Log.d(TAG, "SHOW_GAME result of invite[" + inviteId + "]");
            JSONObject result = new JSONObject(data);
            if (!result.has("error")){
                // Game exists, get inviter's name to show notification:
                dataServer.sendRequest(
                        ServerCommand.SHOW_ACCOUNT,
                        ImmutableMap.of("id", String.valueOf(inviterId)),
                        new HttpConnector.Callback() {
                            @Override
                            public void handleResult(String data) {
                                Log.d(TAG, "SHOW_ACCOUNT result of invite[" + inviteId + "]");
                                try {
                                    JSONObject result = new JSONObject(data);
                                    if (!result.has("error")) {
                                        showGameInviteNotification(inviteId, result.getString("name"), gameId);
                                        // Delete invite so it isn't shown again
                                        deleteInvite(inviteId);
                                    } else {
                                        Log.e(TAG, "Server error while receiving inviter's name.");
                                        gameInviteFinished();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON error while receiving inviter's name.");
                                    gameInviteFinished();
                                }
                            }
                        }
                );
            }else{
                // Game doesn't exist anymore => delete invite
                deleteInvite(inviteId);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON error while receiving game-info.");
            gameInviteFinished();
        }
    }

    private void deleteInvite(final int inviteId){
        dataServer.sendRequest(
                ServerCommand.DELETE_INVITE,
                ImmutableMap.of(
                        "id", String.valueOf(profile.getId()),
                        "token", String.valueOf(profile.getToken()),
                        "inviteId", String.valueOf(inviteId)),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        Log.d(TAG, "DELETE_INVITE result of invite[" + inviteId + "]");
                        gameInviteFinished();
                    }
                }
        );
    }

    private void handleShowAccount(String data){
        try {
            Log.d(TAG, "SHOW_ACCOUNT result");
            JSONObject result = new JSONObject(data);
            if (!result.has("error")) {
                FriendList friendList = new FriendList(result.getJSONArray("friends"));
                totalFriendInvites = 0;
                for(Friend friend : friendList){
                    if(friend.isInviter() && friend.isPending()){
                        // get friend's name to show notification:
                        totalFriendInvites++;
                        final long friendId = friend.getId();
                        dataServer.sendRequest(
                                ServerCommand.SHOW_ACCOUNT,
                                ImmutableMap.of("id", String.valueOf(friendId)),
                                new HttpConnector.Callback() {
                                    @Override
                                    public void handleResult(String data) {
                                        try{
                                            JSONObject result = new JSONObject(data);
                                            if(!result.has("error")){
                                                showFriendInviteNotification(friendId, result.getString("name"));
                                            }else{
                                                Log.e(TAG, "Server error while receiving friend's name.");
                                            }
                                        }catch (JSONException e){
                                            Log.e(TAG, "JSON error while receiving friend's name.");
                                        }
                                        friendInviteFinished();
                                    }
                                }
                        );
                    }
                }
                if(totalFriendInvites==0) friendInvitesFinished(); // Make sure service stops itself even when there are no friend invites
            } else {
                Log.e(TAG, "Server error while receiving friendInvites.");
                friendInvitesFinished();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON error while receiving friendInvites.");
            friendInvitesFinished();
        }
    }

    /**
     * This is called on 2.0+ (API level 5 or higher). Returning
     * START_NOT_STICKY tells the system to not restart the service if it is
     * killed because of poor resource (memory/cpu) conditions.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started.");
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    /**
     * In onDestroy() we release our wake lock. This ensures that whenever the
     * Service stops (killed for resources, stopSelf() called, etc.), the wake
     * lock will be released.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
        profile = null;
        mWakeLock.release();
    }

    private void gameInviteFinished(){
        handledGameInvites++;
        if(totalGameInvites==handledGameInvites)
            gameInvitesFinished();
    }
    private void friendInviteFinished(){
        handledFriendInvites++;
        if(totalFriendInvites==handledFriendInvites)
            friendInvitesFinished();
    }
    private void gameInvitesFinished(){
        gameInvitesReceived = true;
        safeStop();
    }
    private void friendInvitesFinished(){
        friendInvitesReceived = true;
        safeStop();
    }
    /**
     * Safely stop service. Only stop when friends & game invites were received (or at least
     * we tried to receive them).
     */
    private void safeStop(){
        if(gameInvitesReceived && friendInvitesReceived)
            stopSelf();
    }

    /**
     * Show a notification to the user.
     * @param inviteId The id of the invite
     * @param inviterName The name of the user who invited this player
     * @param gameId The id of the game to which the user is invited
     */
    private void showGameInviteNotification(int inviteId, String inviterName, int gameId){
        Log.d(TAG, "Show new game notification: " + inviteId + "," + inviterName + "," + gameId);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New game invite")
                .setContentText(inviterName + " invited you to join his game.")
                .setAutoCancel(true);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the previous intents to the stack:
        Intent mainIntent = new Intent(this, MainActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        // Adds the Intent that starts the Activity to the top of the stack
        Intent lobbyIntent = new Intent(this, LobbyActivity.class);
        lobbyIntent.putExtra(LobbyActivity.JOIN_GAME_EXTRA, gameId);

        stackBuilder.addNextIntent(lobbyIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(GAME_NOTIFICATION_ID + inviteId, mBuilder.build());
    }

    /**
     * Show a notification to the user.
     * @param friendId The id of the friend who sent an invite
     */
    private void showFriendInviteNotification(long friendId, String friendName){
        Log.d(TAG, "Show new friend notification: " + friendId);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New friend invite")
                .setContentText(friendName + " sent you a friend request.")
                .setAutoCancel(true);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the previous intents to the stack
        Intent mainIntent = new Intent(this, MainActivity.class);
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        Bundle profileExtra = new Bundle();
        profileExtra.putParcelable(ProfileActivity.PROFILE_EXTRA, profile);
        profileIntent.putExtra(ProfileActivity.DATA_EXTRA, profileExtra);
        stackBuilder.addNextIntent(mainIntent)
                    .addNextIntent(profileIntent);
        // Adds the Intent that starts the Activity to the top of the stack
        Intent friendsListIntent = new Intent(this, FriendsListActivity.class);
        Bundle data = new Bundle();
        data.putParcelable(FriendsListActivity.PROFILE_EXTRA, profile);
        data.putString(FriendsListActivity.TITLE_EXTRA, "Friend List");
        data.putBoolean(FriendsListActivity.FROM_NOTIFICATION_EXTRA, true);
        friendsListIntent.putExtra(FriendsListActivity.DATA_EXTRA, data);

        stackBuilder.addNextIntent(friendsListIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(FRIEND_NOTIFICATION_ID + (int)friendId, mBuilder.build());
    }
}