package cwa115.trongame.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

/**
 * Code inspiration from http://it-ride.blogspot.be/2010/10/android-implementing-notification.html
 */
public class AppReceiver extends BroadcastReceiver {

    private final static int REPEAT_MINUTES = 5;
    private final static int FIRE_DELAY_SECONDS = 30;
    private final static String TAG = "AppReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // in our case intent will always be CONNECTIVITY_ACTION, so we can just set
        // the alarm
        // Note that a BroadcastReceiver is *NOT* a Context. Thus, we can't use
        // "this" whenever we need to pass a reference to the current context.
        // Thankfully, Android will supply a valid Context as the first parameter
        Log.d(TAG, "intent received");
        scheduleService(context);
    }

    public static void scheduleService(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, NotificationService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        am.cancel(pi);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork!=null && activeNetwork.isConnected()) {
            Log.d(TAG, "Connected to internet => setRepeating service call");
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + FIRE_DELAY_SECONDS * 1000,
                    REPEAT_MINUTES * 60 * 1000, pi);
        }
    }
}