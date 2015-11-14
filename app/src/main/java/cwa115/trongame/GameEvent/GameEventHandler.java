package cwa115.trongame.GameEvent;

import android.widget.Toast;

import com.google.android.gms.games.Game;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cwa115.trongame.GameActivity;
import cwa115.trongame.Network.SocketIoConnection;

/**
 * Created by Peter on 12/11/2015.
 * Handles the game events
 */
public class GameEventHandler {
    private static final int RESULT_TIMEOUT = 1;    // in seconds

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();

    private EventUpdateHandler eventUpdateHandler;
    private GameActivity gameActivity;

    private GameEvent currentEvent;
    private ArrayList<JSONObject> results;

    public GameEventHandler (SocketIoConnection connection, GameActivity gameActivity) {
        this.gameActivity = gameActivity;
        this.eventUpdateHandler = new EventUpdateHandler(connection, this);
    }

    private GameEvent getEvent(String eventType) {
        switch (eventType) {
            case "king_of_hill":
                return new KingOfHillEvent();
            default:
                return null;
        }
    }

    private void addPendingEvent(final GameEvent event) {
        eventUpdateHandler.broadCastEventStart(event);
        Runnable taskEnd = new Runnable() {
            @Override
            public void run() {
                eventUpdateHandler.broadCastEventEnd(event);
            }
        };
        Runnable taskProcess = new Runnable() {
            @Override
            public void run() {
                processResults();
            }
        };
        worker.schedule(taskEnd, event.getTime(), TimeUnit.SECONDS);
        worker.schedule(taskProcess, event.getTime()+RESULT_TIMEOUT, TimeUnit.SECONDS);
    }

    private void processResults() {
        ArrayList<EventResult> scores = currentEvent.calculateResults(results);
        for (EventResult score: scores) {
            eventUpdateHandler.sendEventScore(score.playerId, score.score);
        }
    }

    private void endEvent(GameEvent event) {
        JSONObject result = event.collectData(gameActivity);
        eventUpdateHandler.sendEventResult(event.getEventType(), result);
    }

    public void startEvent(String eventType) {
        if (currentEvent != null) {
            currentEvent = getEvent(eventType);
            String notification = currentEvent.getNotification(gameActivity);
            gameActivity.showNotification(notification, Toast.LENGTH_LONG);
        } else {
            // TODO panic or multiple events?
        }
    }
}
