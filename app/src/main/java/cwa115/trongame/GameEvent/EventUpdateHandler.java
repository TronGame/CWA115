package cwa115.trongame.GameEvent;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Game.GameUpdateHandler;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.Network.SocketIoHandler;
import cwa115.trongame.Utils.LatLngConversion;

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
        public static final String EVENT_RESULT_MESSAGE = "sendResult";
        public static final String PLAYER_ID = "playerId";
        public static final String EVENT_TYPE = "eventType";
    }

    public EventUpdateHandler(SocketIoConnection socket, GameEventHandler gameEventHandler) {
        this.socket = socket;
        this.gameEventHandler = gameEventHandler;

        socket.addSocketIoHandler(this);
    }
    /**
     * Deal with messages from the socket
     * @param message The message
     * @return did the message get handled successfully?
     */
    @Override
    public boolean onMessage(JSONObject message) {
        try {
            switch(message.getString(Protocol.MESSAGE_TYPE)) {
                case Protocol.START_EVENT_MESSAGE:
                    // Start the event
                    onEventStart(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.EVENT_TYPE)
                    );
                    break;
                case Protocol.EVENT_RESULT_MESSAGE:
                    // TODO Send the event result
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


    public void broadCastEventStart(GameEvent event) {
        JSONObject eventStartMesssage = new JSONObject();
        try {
            eventStartMesssage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            eventStartMesssage.put(Protocol.EVENT_TYPE, event.getEventType());

        } catch (JSONException e) {
            // end of the world
        }
        socket.sendMessage(eventStartMesssage, Protocol.START_EVENT_MESSAGE);
    }

    public void onEventStart(String playerId, String eventType) {
        // TODO check if playerId comes from host
        gameEventHandler.startEvent(eventType);
    }

    public void broadCastEventEnd(GameEvent event) {

    }

    public void sendEventResult(String eventType, JSONObject result) {
        try {
            result.put(Protocol.EVENT_TYPE, eventType);
            result.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
        } catch(JSONException e) {
            // end of the world
        };
        socket.sendMessage(result, Protocol.EVENT_RESULT_MESSAGE);
    }

    public void sendEventScore(String playerId, double score) {

    }

}
