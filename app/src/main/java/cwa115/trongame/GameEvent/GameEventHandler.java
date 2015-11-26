package cwa115.trongame.GameEvent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.GameActivity;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.R;

/**
 * Created by Peter on 12/11/2015.
 * Handles the game events
 */
public class GameEventHandler {
    // The time the other players have to send in their results, in seconds
    private static final int RESULT_TIMEOUT = 1;

    // Can execute functions after a certain time
    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();
    private static Handler timerHandler;

    private EventUpdateHandler eventUpdateHandler;          // Handles the socket functionality
    private GameActivity gameActivity;                      // The game activity

    private GameEvent currentEvent;                         // The currently running event
    private ArrayList<JSONObject> results;                  // The results of the current event


    public GameEventHandler (SocketIoConnection connection, GameActivity gameActivity) {
        this.gameActivity = gameActivity;
        this.eventUpdateHandler = new EventUpdateHandler(connection, this);
    }

    public void start() {
        // TODO add random time here
        int time = 2;
        timerHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                addPendingEvent(new KingOfHillEvent());
            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                timerHandler.sendMessage(message);
            }
        };
        worker.schedule(task, time, TimeUnit.SECONDS);
    }

    /**
     * Get a gameEvent object of a certain type
     * @param eventType The type of the event
     * @return The game event of type eventType
     */
    private GameEvent getEvent(String eventType) {
        switch (eventType) {
            case "king_of_hill":
                return new KingOfHillEvent();
            default:
                return null;
        }
    }

    /**
     * Start an event (used by the host)
     * @param event The event to start
     */
    private void addPendingEvent(final GameEvent event) {
        if (GameSettings.isOwner()) {
            final String eventType = event.getEventType();
            timerHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    eventUpdateHandler.broadCastEventEnd(message.getData().getString("eventType"));
                }
            };
            eventUpdateHandler.broadCastEventStart(eventType);
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("eventType", eventType);
                    message.setData(bundle);
                    timerHandler.sendMessage(message);
                }
            };
            worker.schedule(task, event.getTime(), TimeUnit.SECONDS);
        }
    }

    /**
     * Process the results of an event
     */
    private void processResults() {
        ArrayList<EventResult> scores = currentEvent.calculateResults(results);
        for (EventResult score: scores) {
            eventUpdateHandler.sendEventScore(score.playerId, score.score);
        }
        results = null;
        currentEvent = null;
    }

    /**
     * Store the result of a player
     * @param playerId The id of the player
     * @param result The result of the player
     */
    public void storeResult(int playerId, JSONObject result) {
        List<Integer> players = GameSettings.getPlayersInGame();
        if (!players.contains(playerId)) {
            Log.d("GameEvents", "got result from player that is not in this game: " + playerId);
            return;
        }

        if (GameSettings.isOwner()) {
            results.add(result);
        }
    }

    /**
     * End an event. Is called on receiving an end event message from the socket.
     * @param ownerId The id of the sender of the message
     * @param eventType The type of the event
     */
    public void endEvent(int ownerId, String eventType) {
        if (ownerId == GameSettings.getOwner()) {
            if (currentEvent != null && currentEvent.getEventType().equals(eventType)) {
                JSONObject result = currentEvent.collectData(gameActivity);
                eventUpdateHandler.sendEventResult(GameSettings.getUserId(), result);
                if (GameSettings.isOwner()) {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            processResults();
                        }
                    };
                    worker.schedule(task, RESULT_TIMEOUT, TimeUnit.SECONDS);
                }
            } else {
                // TODO panic
            }
        }
    }

    /**
     * Start a new event. Is called on receiving a start event message from the socket
     * @param ownerId The id of the sender of the message
     * @param eventType The type of the event
     */
    public void startEvent(int ownerId, String eventType) {
        if (ownerId == GameSettings.getOwner()) {
            if (currentEvent != null) {
                currentEvent = getEvent(eventType);
                String notification = currentEvent.getNotification(gameActivity);
                gameActivity.showNotification(notification, Toast.LENGTH_LONG);
            } else {
                // TODO panic or multiple events?
            }
        }
    }

    /**
     * Add the score earned in an event
     * @param ownerId The id of the sender of the message
     * @param playerId The id of the player that has to receive the score
     * @param score The score the player has to receive
     */
    public void addScore(int ownerId, int playerId, int score) {
        if (ownerId == GameSettings.getOwner()) {
            if (playerId == GameSettings.getUserId()) {
                gameActivity.addScore(score);
                // TODO also show place in ranking
                gameActivity.showNotification(
                        gameActivity.getString(R.string.event_won_text) +" "+
                        gameActivity.getString(R.string.score_received_text).replaceAll("%score", ""+score),
                        Toast.LENGTH_LONG
                );
            }
        }
    }
}
