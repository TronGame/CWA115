package cwa115.trongame.Network;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;

import org.json.JSONException;
import org.json.JSONObject;

import cwa115.trongame.GameSettings;
import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * Created by Peter on 10/11/2015.
 */
public class GameUpdateHandler implements SocketIoHandler {
    private SocketIoConnection socket;
    private Map map;
    private String myId;
    private GeoApiContext context;                      // The context that takes care of the location snapping


    public GameUpdateHandler(String playerId, String groupId, String sessionId, Map map, GeoApiContext context) {
        this.map = map;
        this.myId = playerId;
        this.context = context;
        this.socket = new SocketIoConnection(groupId, sessionId, this);
    }

    public boolean onMessage(JSONObject message) {
        try {
            switch(message.getString("type")) {
                case "updatePosition":
                    onRemoteLocationChange(
                            message.getString("playerId"),
                            message.getString("playerName"),
                            LatLngConversion.getPointFromJSON(message.getJSONObject("location"))
                    );
                    break;
                case "updateWall":
                    onRemoteWallUpdate(
                            message.getString("playerId"),
                            message.getString("wallId"),
                            LatLngConversion.getPointFromJSON(message.getJSONObject("point"))
                    );
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
        if (myId.equals(playerId))
            return;

        // Check if we alread know about the player
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
        if(myId.equals(playerId))
            return; // We sent this ourselves
        if(!map.hasObject(wallId)) {
            map.addMapItem(new Wall(wallId, playerId, new LatLng[]{point}, context));
            Log.d("SERVER", "New wall created by " + playerId + " at " + point.toString());
        } else {
            Wall remoteWall = (Wall)map.getItemById(wallId);
            if(!remoteWall.getOwnerId().equals(playerId))
                return;
            remoteWall.addPoint(point);
            Log.d("SERVER", "Update wall " + wallId + " of " + playerId + " by " + point.toString());
            map.redraw(remoteWall.getId());
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
            locationMessage.put("playerId", myId);
            locationMessage.put("playerName", GameSettings.getPlayerName());
            locationMessage.put("location", LatLngConversion.getJSONFromPoint(location));
        } catch (JSONException e) {
            // end of the world
        }

        // Send the message over the socket
        socket.sendMessage(locationMessage, "updatePosition");
    }

    /**
     * Send an extra wall point to the other players.
     * @param point the point to be remotely added to the wall
     */
    public void sendUpdateWall(LatLng point, String wallId) {
        JSONObject updateWallMessage = new JSONObject();
        try {
            updateWallMessage.put("playerId", myId);
            updateWallMessage.put("wallId", wallId);
            updateWallMessage.put("point", LatLngConversion.getJSONFromPoint(point));
        } catch(JSONException e) {
            // end of the world
        }
        socket.sendMessage(updateWallMessage, "updateWall");
    }
}
