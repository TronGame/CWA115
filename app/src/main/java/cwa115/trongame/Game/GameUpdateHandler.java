package cwa115.trongame.Game;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.Network.SocketIoHandler;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * Created by Peter on 10/11/2015.
 * Deals with updates from the other players
 */
public class GameUpdateHandler implements SocketIoHandler {
    private SocketIoConnection socket;                  // The socket connection
    private Map map;                                    // The map object to draw the objects on
    private GeoApiContext context;                      // The context that takes care of the location snapping

    /**
     * Contains protocol constants (i.e. JSON field names).
     */
    public class Protocol {
        public static final String MESSAGE_TYPE = "type";
        public static final String UPDATE_POSITION_MESSAGE = "updatePosition";
        public static final String UPDATE_WALL_MESSAGE = "updateWall";
        public static final String CREATE_WALL_MESSAGE = "createWall";

        public static final String PLAYER_ID = "playerId";
        public static final String PLAYER_NAME = "playerName";
        public static final String PLAYER_LOCATION = "location";

        public static final String WALL_ID = "wallId";
        public static final String WALL_POINT = "point";
        public static final String WALL_POINTS = "points";
        public static final String WALL_COLOR = "color";
    }

    public GameUpdateHandler(SocketIoConnection socket, Map map, GeoApiContext context) {
        this.map = map;
        this.context = context;
        this.socket = socket;

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
                case Protocol.UPDATE_POSITION_MESSAGE:
                    // Update the position of the player
                    onRemoteLocationChange(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.PLAYER_NAME),
                            LatLngConversion.getPointFromJSON(
                                    message.getJSONObject(Protocol.PLAYER_LOCATION)
                            )
                    );
                    break;
                // Add a point to a wall
                case Protocol.UPDATE_WALL_MESSAGE:
                    onRemoteWallUpdate(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.WALL_ID),
                            LatLngConversion.getPointFromJSON(
                                    message.getJSONObject(Protocol.WALL_POINT)
                            )
                    );
                    break;
                // Create a new wall
                case Protocol.CREATE_WALL_MESSAGE:
                    onRemoteWallCreated(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.WALL_ID),
                            message.getInt(Protocol.WALL_COLOR),
                            LatLngConversion.getPointsFromJSON(
                                    message.getJSONObject(Protocol.WALL_POINTS)
                            )
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

    /**
     * Change the location of another player
     *
     * @param playerId The id of the updated player
     * @param location The location of the updated player
     */
    public void onRemoteLocationChange(String playerId, String playerName, LatLng location) {
        // Check if the message we received originates from ourselves
        if (GameSettings.getPlayerId().equals(playerId))
            return;

        // Check if we already know about the player
        if (map.hasObject(playerId)) {
            // Update the location of the player
            map.updatePlayer(playerId, location);
        } else {
            // Add player to map
            map.addMapItem(new Player(playerId, playerName, location));
        }

        Log.d("SERVER", "Location of " + playerId + " updated to " + location.toString());
    }

    /**
     * Called when a remote wall is extended by one point, or created
     * @param playerId the player identifier of the wall owner
     * @param wallId the identifier of the wall
     * @param point the newly added point
     */
    public void onRemoteWallUpdate(String playerId, String wallId, LatLng point) {
        if(GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        if(map.hasObject(wallId)) {
            Wall remoteWall = (Wall)map.getItemById(wallId);
            if(!remoteWall.getOwnerId().equals(playerId))
                return;
            remoteWall.addPoint(point);
            Log.d("SERVER", "Update wall " + wallId + " of " + playerId + " by " + point.toString());
            map.redraw(remoteWall.getId());
        }
    }

    /**
     * Called when a remote wall is created
     * @param playerId the player identifier of the wall owner
     * @param wallId the identifier of the wall
     * @param color the color of the wall
     * @param points the points of the wall
     */
    public void onRemoteWallCreated(String playerId, String wallId, int color, ArrayList<LatLng> points) {
        if(GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        if(!map.hasObject(wallId)) {
            map.addMapItem(new Wall(wallId, playerId, color, points, context));
            map.redraw(wallId);
            Log.d("SERVER", "New wall created by " + playerId);
        }
    }

    /**
     * Send the current player location
     *
     * @param location the location of the player
     */
    public void sendMyLocation(LatLng location) {
        // Create the message that will be send over the socket connection
        JSONObject locationMessage = new JSONObject();
        try {
            locationMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            locationMessage.put(Protocol.PLAYER_NAME, GameSettings.getPlayerName());
            locationMessage.put(Protocol.PLAYER_LOCATION, LatLngConversion.getJSONFromPoint(location));
        } catch (JSONException e) {
            // end of the world
        }

        // Send the message over the socket
        socket.sendMessage(locationMessage, Protocol.UPDATE_POSITION_MESSAGE);
    }

    /**
     * Send an extra wall point to the other players.
     * @param point the point to be remotely added to the wall
     */
    public void sendUpdateWall(LatLng point, String wallId) {
        JSONObject updateWallMessage = new JSONObject();
        try {
            updateWallMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            updateWallMessage.put(Protocol.WALL_ID, wallId);
            updateWallMessage.put(Protocol.WALL_POINT, LatLngConversion.getJSONFromPoint(point));
        } catch(JSONException e) {
            // end of the world
        }
        socket.sendMessage(updateWallMessage, Protocol.UPDATE_WALL_MESSAGE);
    }

    /**
     * Create a new wall on the other devices
     * @param wallId The id of the newly created wall
     * @param points The points inside of the newly created wall
     * @param color The color of the newly created wall
     */
    public void sendCreateWall(String wallId, ArrayList<LatLng> points, int color) {
        JSONObject createWallMessage = new JSONObject();
        try {
            createWallMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            createWallMessage.put(Protocol.WALL_ID, wallId);
            createWallMessage.put(Protocol.WALL_COLOR, color);
            createWallMessage.put(Protocol.WALL_POINTS, LatLngConversion.getJSONFromPoints(points));

        } catch(JSONException e) {
            // end of the world
        }
        socket.sendMessage(createWallMessage, Protocol.CREATE_WALL_MESSAGE);
    }
}
