package cwa115.trongame.GameEvent;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.Network.SocketIoHandler;

/**
 * Created by Peter on 12/11/2015.
 */
public class EventUpdateHandler implements SocketIoHandler {
    private GameEventHandler gameEventHandler;          // The game event handler
    private SocketIoConnection socket;                  // The socket connection

    /**
     * Contains protocol constants (i.e. JSON field names).
     */
    public class Protocol {
        public static final String MESSAGE_TYPE = "type";
        public static final String START_EVENT_MESSAGE = "startEvent";
        public static final String END_EVENT_MESSAGE = "endEvent";
        public static final String EVENT_RESULT_MESSAGE = "sendResult";
        public static final String EVENT_SCORE_MESSAGE = "scoreMessage";
        public static final String PLAYER_ID = "playerId";
        public static final String OWNER_ID = "ownerId";
        public static final String EVENT_TYPE = "eventType";
        public static final String EVENT_SCORE = "score";
    }

    public EventUpdateHandler(SocketIoConnection socket, GameEventHandler gameEventHandler) {
        this.socket = socket;
        this.gameEventHandler = gameEventHandler;

        socket.addSocketIoHandler(this);
    }

    // Receiving Messages
    // ---------------------------------------------------------------------------------------------
    /**
     * Deal with messages from the socket
     * @param message The message
     * @return did the message get handled successfully?
     */
    @Override
    public boolean onMessage(JSONObject message) {
        // TODO check if messages come from the game host
        try {
            switch(message.getString(Protocol.MESSAGE_TYPE)) {
                case Protocol.START_EVENT_MESSAGE:
                    // Start the event
                    gameEventHandler.startEvent(
                            Integer.valueOf(message.getString(Protocol.OWNER_ID)),
                            message.getString(Protocol.EVENT_TYPE)
                    );
                    break;
                case Protocol.END_EVENT_MESSAGE:
                    // End the event
                    // Calculate the event result and send the information to the host
                    gameEventHandler.endEvent(
                            Integer.valueOf(message.getString(Protocol.OWNER_ID)),
                            message.getString(Protocol.EVENT_TYPE)
                    );
                    break;
                case Protocol.EVENT_SCORE_MESSAGE:
                    // Check if the score is yours and add it.
                    gameEventHandler.addScore(
                            Integer.valueOf(message.getString(Protocol.OWNER_ID)),
                            Integer.valueOf(message.getString(Protocol.PLAYER_ID)),
                            Integer.valueOf(message.getString(Protocol.EVENT_SCORE)));
                    break;

                case Protocol.EVENT_RESULT_MESSAGE:
                    // Store the result of an event
                    gameEventHandler.storeResult(
                            Integer.valueOf(message.getString(Protocol.PLAYER_ID)),
                            message
                    );
                    break;
                default:
                    break;
            }

            return true;
        } catch(JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Sending Messages
    // ---------------------------------------------------------------------------------------------

    // Main functionality

    /**
     * Send the result of an event. This will be used to calculate your score
     * @param playerId The id of the player that send the result
     * @param result The result of the event
     */
    public void sendEventResult(int playerId, JSONObject result) {
        try {
            result.put(Protocol.PLAYER_ID, playerId);
        } catch(JSONException e) {
            // end of the world
        };
        socket.sendMessage(result, Protocol.EVENT_RESULT_MESSAGE);
    }

    // Host functionality

    /**
     * Tell all of the players that the event has come to an end
     * @param eventType The type of the event that has ended
     */
    public void broadCastEventEnd(String eventType) {
        JSONObject eventEndMesssage = new JSONObject();
        try {
            eventEndMesssage.put(Protocol.OWNER_ID, GameSettings.getUserId());
            eventEndMesssage.put(Protocol.EVENT_TYPE, eventType);

        } catch (JSONException e) {
            // end of the world
        }
        socket.sendMessage(eventEndMesssage, Protocol.END_EVENT_MESSAGE);
    }

    /**
     * Tell all of the players that a new event has started
     * @param eventType The type of the event that has started
     */
    public void broadCastEventStart(String eventType) {
        JSONObject eventStartMesssage = new JSONObject();
        try {
            eventStartMesssage.put(Protocol.OWNER_ID, GameSettings.getUserId());
            eventStartMesssage.put(Protocol.EVENT_TYPE, eventType);

        } catch (JSONException e) {
            // end of the world
        }
        socket.sendMessage(eventStartMesssage, Protocol.START_EVENT_MESSAGE);
    }

    /**
     * Distribute the scores after the event has finished
     * @param playerId The id of the player who has to receive the score
     * @param score The score the player has to receive.
     */
    public void sendEventScore(int playerId, double score) {
        JSONObject scoreMessage = new JSONObject();
        try {
            scoreMessage.put(Protocol.OWNER_ID, GameSettings.getUserId());
            scoreMessage.put(Protocol.PLAYER_ID, playerId);
            scoreMessage.put(Protocol.EVENT_SCORE, score);
        } catch(JSONException e) {
            // end of the world
        };
        socket.sendMessage(scoreMessage, Protocol.EVENT_RESULT_MESSAGE);
    }
}
