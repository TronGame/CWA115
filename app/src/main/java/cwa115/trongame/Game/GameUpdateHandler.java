package cwa115.trongame.Game;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cwa115.trongame.GameActivity;
import cwa115.trongame.Game.Map.Map;
import cwa115.trongame.Game.Map.Player;
import cwa115.trongame.Game.Map.Wall;
import cwa115.trongame.Network.Socket.SocketIoConnection;
import cwa115.trongame.Network.Socket.SocketIoHandler;
import cwa115.trongame.R;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * Created by Peter on 10/11/2015.
 * Deals with updates from the other players
 */
public class GameUpdateHandler implements SocketIoHandler {
    private SocketIoConnection socket;                  // The socket connection
    private Map map;                                    // The map object to draw the objects on
    private GameActivity gameActivity;                  // The game activity
    private GeoApiContext context;                      // The context that takes care of the location snapping

    /**
     * Contains protocol constants (i.e. JSON field names).
     */
    public class Protocol {
        public static final String MESSAGE_TYPE = "type";
        public static final String UPDATE_POSITION_MESSAGE = "updatePosition";
        public static final String PLAYER_DEAD_MESSAGE = "playerDead";
        public static final String UPDATE_WALL_MESSAGE = "updateWall";
        public static final String CREATE_WALL_MESSAGE = "createWall";
        public static final String REMOVE_WALL_MESSAGE = "removeWall";
        public static final String END_GAME_MESSAGE = "endGame";
        public static final String START_GAME_MESSAGE = "startGame";
        public static final String FINAL_SCORE_MESSAGE = "finalScore";
        public static final String WINNER_MESSAGE = "winner";

        public static final String PLAYER_ID = "playerId";
        public static final String PLAYER_NAME = "playerName";
        public static final String PLAYER_LOCATION = "location";
        public static final String PLAYER_KILLER_ID = "playerKillerId";
        public static final String PLAYER_KILLER_NAME = "playerKillerName";

        public static final String START_LOCATION = "startLocation";
        public static final String BORDER_SIZE = "borderSize";

        public static final String WALL_ID = "wallId";
        public static final String WALL_OWNER_ID = "wallOwnerId";
        public static final String WALL_POINT = "point";
        public static final String WALL_POINTS = "points";
        public static final String WALL_COLOR = "color";

        public static final String FINAL_SCORE = "totalScore";
        public static final String WINNER_ID = "winnerId";
        public static final String BELL_MESSAGE = "ringBell";
    }

    public GameUpdateHandler(GameActivity gameActivity, SocketIoConnection socket, Map map, GeoApiContext context) {
        this.map = map;
        this.context = context;
        this.socket = socket;
        this.gameActivity = gameActivity;

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
                case Protocol.PLAYER_DEAD_MESSAGE:
                    // Tell GameActivity that the player has died
                    onPlayerDead(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.PLAYER_NAME),
                            message.getString(Protocol.PLAYER_KILLER_ID),
                            message.getString(Protocol.PLAYER_KILLER_NAME)
                    );
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
                            message.getString(Protocol.WALL_OWNER_ID),
                            message.getString(Protocol.WALL_ID),
                            message.getInt(Protocol.WALL_COLOR),
                            LatLngConversion.getPointsFromJSON(
                                    message.getJSONObject(Protocol.WALL_POINTS)
                            )
                    );
                    break;
                // Remove a wall
                case Protocol.REMOVE_WALL_MESSAGE:
                    onRemoteWallRemoved(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.WALL_ID)
                    );
                    break;
                // Start the game
                case Protocol.START_GAME_MESSAGE:
                    onStartGameMessage(
                            message.getString(Protocol.PLAYER_ID),
                            LatLngConversion.getPointFromJSON(
                                    message.getJSONObject(Protocol.START_LOCATION)
                            )
                    );
                    break;
                // End the game
                case Protocol.END_GAME_MESSAGE:
                    onEndGameMessage(
                            message.getString(Protocol.PLAYER_ID)
                    );
                    break;
                // Store the final score
                case Protocol.FINAL_SCORE_MESSAGE:
                    onFinalScore(
                            message.getString(Protocol.PLAYER_ID),
                            message.getDouble(Protocol.FINAL_SCORE)
                    );
                    break;
                // Show the user the winner of the game
                case Protocol.WINNER_MESSAGE:
                    onWinner(
                            message.getString(Protocol.PLAYER_ID),
                            message.getString(Protocol.WINNER_ID)
                    );
                    break;
                // Remote bell
                case Protocol.BELL_MESSAGE:
                    onBellSound(message.getString(Protocol.PLAYER_ID));
                default:
                    break;

            }

            return true;
        } catch(JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    // region Receiving messages
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
     * Called when a player has died
     * @param playerId The id of the player that died
     * @param killerId The id of the player that killed the dead player (is null when the player died for another reason)
     */
    private void onPlayerDead(String playerId, String playerName, String killerId, String killerName) {
        if(GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        gameActivity.playerDied(playerName, killerId, killerName);
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
    public void onRemoteWallCreated(String playerId, String ownerId, String wallId, int color, ArrayList<LatLng> points) {
        if(GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        if(!map.hasObject(wallId)) {
            map.addMapItem(new Wall(wallId, playerId, color, points, context));
            map.redraw(wallId);
            Log.d("SERVER", "New wall created by " + playerId);
        } else {
            // Remove the old wall
            map.removeMapItem(wallId);
            // Update the wall
            map.addMapItem(new Wall(wallId, playerId, color, points, context));
            map.redraw(wallId);
            Log.d("SERVER", "Wall updated by " + playerId);
        }
    }

    /**
     * Clears a wall from the map. Note: this doesn't remove the wall from the mapItems list.
     * @param playerId The player that issued the command
     * @param wallId The wall that has to be removed
     */
    public void onRemoteWallRemoved(String playerId, String wallId) {
        if(GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        if(!map.hasObject(wallId)) {
            map.clear(wallId);
        }
    }

    /**
     * End the game
     * @param ownerId The sender of the message
     */
    public void onEndGameMessage(String ownerId) {
        if (ownerId.equals(GameSettings.getPlayerId()))
            return; // We sent this ourselves

        if (!GameSettings.isOwner() && GameSettings.getOwner() == Integer.valueOf(ownerId)) {
            gameActivity.sendScore();
        }
    }

    /**
     * Start the game
     * @param ownerId The id of the sender (this has to be the owner)
     */
    public void onStartGameMessage(String ownerId, LatLng position) {
        if (ownerId.equals(GameSettings.getPlayerId()))
            return; // We sent this ourselves

        if (!GameSettings.isOwner() && GameSettings.getOwner() == Integer.valueOf(ownerId)) {
            gameActivity.startGame(position);
        }
    }

    /**
     * Process a final score send by another player
     * @param playerId The player
     * @param score The score of that player
     */
    public void onFinalScore(String playerId, double score) {
        if (GameSettings.getPlayerId().equals(playerId))
            return; // We sent this ourselves
        if (GameSettings.isOwner())
            gameActivity.storePlayerScore(playerId, score);
    }

    /**
     * Process the winner received from the host
     * @param ownerId The sender
     * @param winner The winner
     */
    public void onWinner(String ownerId, String winner) {
        if (GameSettings.getPlayerId().equals(ownerId))
            return; // We sent this ourselves

        if (GameSettings.getOwner() == Integer.valueOf(ownerId))
            gameActivity.setWinner(winner);
            gameActivity.showWinner();
    }

    /**
     * Update the bike icon of the player that rang his bell
     * (player id can be the id of the player himself)
     * @param playerId the player that rang the bell
     */
    public void onBellSound(final String playerId) {
        Player remotePlayer = (Player)map.getItemById(playerId);
        if (remotePlayer != null)
            remotePlayer.setCustomMarker(R.mipmap.bell_marker);

        // After 3 seconds, disable the bell.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Player me = (Player)map.getItemById(playerId);
                me.resetMarker();
            }
        }, 3000);
    }

    // endregion

    // region Sending messages

    /**
     * Send the current player location
     * @param location the location of the player
     */
    public void sendMyLocation(LatLng location) {
        if (GameSettings.getSpectate())
            return;

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
     * Tell the other devices that a player has died
     * @param killerId The killer of the player that has died
     * @param killerName The name of the killer that has died
     */
    public void sendDeathMessage(String killerId, String killerName) {
        if (GameSettings.getSpectate())
            return;

        // Create the message that will be send over the socket connection
        JSONObject deathMessage = new JSONObject();
        try {
            deathMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            deathMessage.put(Protocol.PLAYER_NAME, GameSettings.getPlayerName());
            deathMessage.put(Protocol.PLAYER_KILLER_ID, killerId);
            deathMessage.put(Protocol.PLAYER_KILLER_NAME, killerName);
        } catch (JSONException e) {
            // end of the world
        }

        // Send the message over the socket
        socket.sendMessage(deathMessage, Protocol.UPDATE_POSITION_MESSAGE);
    }

    /**
     * Send an extra wall point to the other players.
     * @param point the point to be remotely added to the wall
     */
    public void sendUpdateWall(LatLng point, String wallId) {
        if (GameSettings.getSpectate())
            return;

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
     * @param ownerId the player identifier of the wall owner
     * @param wallId The id of the newly created wall
     * @param points The points inside of the newly created wall
     * @param color The color of the newly created wall
     */
    public void sendCreateWall(String ownerId, String wallId, ArrayList<LatLng> points, int color) {
        if (GameSettings.getSpectate())
            return;

        JSONObject createWallMessage = new JSONObject();
        try {
            createWallMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            createWallMessage.put(Protocol.WALL_OWNER_ID, ownerId);
            createWallMessage.put(Protocol.WALL_ID, wallId);
            createWallMessage.put(Protocol.WALL_COLOR, color);
            createWallMessage.put(Protocol.WALL_POINTS, LatLngConversion.getJSONFromPoints(points));

        } catch(JSONException e) {
            // end of the world
        }
        socket.sendMessage(createWallMessage, Protocol.CREATE_WALL_MESSAGE);
    }

    /**
     * Remove a wall on the other devices
     * @param wallId The id of the wall that has been removed
     */
    public void sendRemoveWall(String wallId) {
        if (GameSettings.getSpectate())
            return;

        JSONObject removeWallMessage = new JSONObject();
        try {
            removeWallMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            removeWallMessage.put(Protocol.WALL_ID, wallId);
        } catch (JSONException e) {

        }
        socket.sendMessage(removeWallMessage, Protocol.REMOVE_WALL_MESSAGE);
    }

    /**
     * Sends the score at the end of the game
     */
    public void sendScore(double score) {
        if (GameSettings.getSpectate())
            return;

        JSONObject scoreMessage = new JSONObject();
        try {
            scoreMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            scoreMessage.put(Protocol.FINAL_SCORE, score);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.sendMessage(scoreMessage, Protocol.FINAL_SCORE_MESSAGE);
    }

    /**
     * Tell the other players that your bell is ringing
     * @param playerId Your id
     */
    public void sendBellSound(String playerId) {
        if (GameSettings.getSpectate())
            return;

        JSONObject bellMessage = new JSONObject();
        try {
            bellMessage.put(Protocol.PLAYER_ID, playerId);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        socket.sendMessage(bellMessage, Protocol.BELL_MESSAGE);
    }

    // Host Messages
    // -----------------

    /**
     * Sends the score at the end of the game
     * @param winner The id of the winner of the game
     */
    public void sendWinner(String winner) {
        JSONObject scoreMessage = new JSONObject();
        try {
            scoreMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            scoreMessage.put(Protocol.WINNER_ID, winner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.sendMessage(scoreMessage, Protocol.WINNER_MESSAGE);
    }

    /**
     * Called by the host when the game has ended
     */
    public void sendEndGame() {
        JSONObject endGameMessage = new JSONObject();
        try {
            endGameMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.sendMessage(endGameMessage, Protocol.END_GAME_MESSAGE);
    }

    /**
     * Tell the other players that the game has started
     */
    public void sendStartGame(LatLng position) {
        JSONObject startGameMessage = new JSONObject();
        try {
            startGameMessage.put(Protocol.PLAYER_ID, GameSettings.getPlayerId());
            startGameMessage.put(Protocol.START_LOCATION, LatLngConversion.getJSONFromPoint(position));
            // Send border size for panel
            startGameMessage.put(Protocol.BORDER_SIZE, GameSettings.getMaxDistanceInMeters());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.sendMessage(startGameMessage, Protocol.START_GAME_MESSAGE);
    }

    // endregion
}
