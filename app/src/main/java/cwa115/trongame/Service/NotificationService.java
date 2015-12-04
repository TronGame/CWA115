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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Friend;
import cwa115.trongame.FriendList;
import cwa115.trongame.FriendsListActivity;
import cwa115.trongame.MainActivity;
import cwa115.trongame.Network.HttpConnector;
import cwa115.trongame.Network.ServerCommand;
import cwa115.trongame.Profile;
import cwa115.trongame.ProfileActivity;
import cwa115.trongame.R;
import cwa115.trongame.RoomActivity;

/**
 * Code inspiration from http://it-ride.blogspot.be/2010/10/android-implementing-notification.html
 */
public class NotificationService extends Service {

    private final static String TAG = "NotificationService";

    private PowerManager.WakeLock mWakeLock;
    private boolean gameInvitesReceived, friendInvitesReceived;
    private Profile profile;

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
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            stopSelf();
            return;
        }

        HttpConnector dataServer = new HttpConnector(getString(R.string.dataserver_url));
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        profile = Profile.Load(settings);
        friendInvitesReceived = false;
        gameInvitesReceived = false;
        // do the actual work, in a separate thread
        dataServer.sendRequest(
                ServerCommand.SHOW_INVITES,
                profile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try{
                            JSONObject result = new JSONObject(data);
                            if(!result.has("error")){
                                JSONArray invites = result.getJSONArray("invites");
                                for(int i=0;i<invites.length();i++){
                                    JSONObject invite = invites.getJSONObject(i);
                                    int inviteId = invite.getInt("inviteId");
                                    int inviterId = invite.getInt("inviterId");
                                    int gameId = invite.getInt("gameId");
                                    showGameInviteNotification(inviteId, inviterId, gameId);
                                }
                            }else
                                Log.e(TAG, "Server error while receiving gameInvites.");
                        }catch (JSONException e){
                            e.printStackTrace();
                            Log.e(TAG, "JSON error while receiving gameInvites.");
                        }
                        gameInvitesReceived = true;
                        safeStop();
                    }
                }
        );
        dataServer.sendRequest(
                ServerCommand.SHOW_ACCOUNT,
                profile.GetQuery(Profile.SERVER_ID_PARAM, Profile.SERVER_TOKEN_PARAM),
                new HttpConnector.Callback() {
                    @Override
                    public void handleResult(String data) {
                        try {
                            JSONObject result = new JSONObject(data);
                            if (!result.has("error")) {
                                FriendList friendList = new FriendList(result.getJSONArray("friends"));
                                for(Friend friend : friendList){
                                    if(friend.isInviter() && friend.isPending())
                                        showFriendInviteNotification(friend.getId());
                                }
                            } else {
                                Log.e(TAG, "Server error while receiving friendInvites.");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON error while receiving friendInvites.");
                        }
                        friendInvitesReceived = true;
                        safeStop();
                    }
                });
    }

    /**
     * This is called on 2.0+ (API level 5 or higher). Returning
     * START_NOT_STICKY tells the system to not restart the service if it is
     * killed because of poor resource (memory/cpu) conditions.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        profile = null;
        mWakeLock.release();
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
     * @param inviterId The id of the user who invited this player
     * @param gameId The id of the game to which the user is invited
     */
    private void showGameInviteNotification(int inviteId, int inviterId, int gameId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New game invite")
                .setContentText("invited you to join his game.");

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class)
                    .addParentStack(RoomActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("gameId", gameId);
        stackBuilder.addNextIntent(new Intent(this, RoomActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(inviteId, mBuilder.build());
    }

    /**
     * Show a notification to the user.
     * @param friendId The id of the friend who sent an invite
     */
    private void showFriendInviteNotification(long friendId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New friend invite")
                .setContentText("sent you a friend request.");

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class)
                .addParentStack(ProfileActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        Intent intent = new Intent(this, FriendsListActivity.class);
        Bundle data = new Bundle();
        data.putParcelable(FriendsListActivity.PROFILE_EXTRA, profile);
        data.putString(FriendsListActivity.TITLE_EXTRA, "Friendlist:");
        intent.putExtra(FriendsListActivity.DATA_EXTRA, data);
        stackBuilder.addNextIntent(new Intent(this, FriendsListActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify((int)friendId, mBuilder.build());
    }
}