package cwa115.trongame.Game.GameEvent;

import android.os.Handler;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.GameActivity;
import cwa115.trongame.Game.GameEvent.Events.BellEvent;
import cwa115.trongame.Game.GameEvent.Events.GameEvent;
import cwa115.trongame.Game.GameEvent.Events.KingOfHillEvent;
import cwa115.trongame.Game.GameEvent.Events.ShowOffEvent;
import cwa115.trongame.Network.Socket.SocketIoConnection;
import cwa115.trongame.R;

/**
 * Created by Peter on 12/11/2015.
 * Handles the game events
 */
public class GameEventHandler {
    // The time the other players have to send in their results, in seconds
    private static final int RESULT_TIMEOUT = 1;
    private static final int MIN_EVENT_DELAY = 0;
    private static final int MAX_EVENT_DELAY = 5*60;

    public static final String[] eventTypes = {
            KingOfHillEvent.EVENT_TYPE, ShowOffEvent.EVENT_TYPE, BellEvent.EVENT_TYPE
    };

    private EventUpdateHandler eventUpdateHandler;          // Handles the socket functionality
    private GameActivity gameActivity;                      // The game activity

    private GameEvent currentEvent;                         // The currently running event
    private ArrayList<JSONObject> results;                  // The results of the current event


    public GameEventHandler (SocketIoConnection connection, GameActivity gameActivity) {
        this.gameActivity = gameActivity;
        this.eventUpdateHandler = new EventUpdateHandler(connection, this);
    }

    public void start() {
        // Get a random event type
        int idx = new Random().nextInt(eventTypes.length);
        final String eventType = (eventTypes[idx]);

        // Get a random time
        int time = MIN_EVENT_DELAY + new Random().nextInt(MAX_EVENT_DELAY - MIN_EVENT_DELAY);

        // Start the event after time seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addPendingEvent(getEvent(eventType));
            }
        }, time * 1000);  // TODO is this time correct?
    }

    /**
     * Get a gameEvent object of a certain type
     * @param eventType The type of the event
     * @return The game event of type eventType
     */
    private GameEvent getEvent(String eventType) {
        switch (eventType) {
            case KingOfHillEvent.EVENT_TYPE:
                return new KingOfHillEvent();
            case ShowOffEvent.EVENT_TYPE:
                return new ShowOffEvent();
            case BellEvent.EVENT_TYPE:
                return new BellEvent();
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
            eventUpdateHandler.broadCastEventStart(eventType);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventUpdateHandler.broadCastEventEnd(eventType);
                }
            }, event.getTime() * 1000);  // TODO is this time correct?
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
        // start another event
        start();
    }

    /**
     * Store the result of a player
     * @param playerId The id of the player
     * @param result The result of the player
     */
    public void storeResult(int playerId, JSONObject result) {
        if (currentEvent != null) {
            List<Integer> players = GameSettings.getPlayersInGame();
            if (!players.contains(playerId)) {
                Toast.makeText(gameActivity, "player: " + playerId + " entered game illegally", Toast.LENGTH_SHORT).show();
                return;
            }

            if (GameSettings.isOwner()) {
                results.add(result);
            }
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

                // The player can only send a result when he is alive
                if (gameActivity.isAlive) {
                    JSONObject result = currentEvent.collectData(gameActivity);
                    eventUpdateHandler.sendEventResult(GameSettings.getUserId(), result);
                }

                // The owner must process the results
                if (GameSettings.isOwner()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            processResults();
                        }
                    }, RESULT_TIMEOUT * 1000);
                } else {
                    // Other players can already reset currentEvent
                    currentEvent = null;
                }
            } else {
                Toast.makeText(gameActivity, "Might be listening on wrong channel", Toast.LENGTH_SHORT).show();
                // TODO deal with this
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
            if (currentEvent == null) {
                if (GameSettings.isOwner())
                    results = new ArrayList<>();
                currentEvent = getEvent(eventType);
                currentEvent.startEvent(gameActivity);
                String notification = currentEvent.getNotification(gameActivity);
                gameActivity.showNotification(notification, Toast.LENGTH_LONG);
            } else {
                Toast.makeText(gameActivity, "Might be listening on wrong channel", Toast.LENGTH_SHORT).show();
                // TODO deal with this
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
        // This can only happen when a player has just send the result before dying
        if (!gameActivity.isAlive)
            return;

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
