package cwa115.trongame.Game;

import java.util.List;
import java.util.UUID;

/**
 * Singleton to manage the settings of the current game.
 * These settings are not persistent.
 */
public final class GameSettings {

    private static String playerName;
    private static int playerMarkerImage;
    private static int wallColor;
    private static String playerToken;
    private static int userId;
    private static int ownerId;
    private static int gameId;
    private static String gameToken;
    private static String gameName;
    private static int maxPlayers;
    private static boolean canBreakWall;

    private static List<Integer> playersInGame;
    
    /**
     * Sets the name of the local player.
     * @param name the name of the player
     */
    public static void setPlayerName(String name) {
        playerName = name;
    }

    /**
     * @return the name of the (local) player
     */
    public static String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the resource id for the default player marker
     * @param markerId the given resource identifier
     */
    public static void setPlayerMarkerImage(int markerId) {
        playerMarkerImage = markerId;
    }

    /**
     * @return the resource identifier for the player marker
     */
    public static int getPlayerMarkerImage() {
        return playerMarkerImage;
    }

    /**
     * @return the color of the wall
     */
    public static int getWallColor() {return wallColor; }

    /**
     * Set the wall color
     * @param color the given color
     */
    public static void setWallColor(int color) {wallColor = color; }

    /**
     * set the max players
     * @param max_nb_Players the max number of players
     */
    public static void setMaxPlayers(int max_nb_Players){
        maxPlayers=max_nb_Players;
    }

    public static int getMaxPlayers(){
        return maxPlayers;
    }


    /**
     * (Randomly) generates a unique ID.
     * @return the generated ID
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    
    public static void setPlayerToken(String token) {
        playerToken = token;
    }

    public static void setUserId(int id) { userId = id; }

    public static int getUserId() {return userId; }

    public static String getPlayerId() {return String.valueOf(userId);}

    public static void setGameId(int gameId) {
        GameSettings.gameId = gameId;
    }

    public static void setGameToken(String gameToken) {
        GameSettings.gameToken = gameToken;
    }

    public static void setGameName(String gameName) {
        GameSettings.gameName = gameName;
    }

    public static String getGameName(){
        return gameName;
    }

    public static String getPlayerToken() {
        return playerToken;
    }

    /**
     * @return Is this player the owner?
     */
    public static boolean isOwner() {
        return ownerId==userId;
    }

    public static void setOwnerId(int id) {
        ownerId = id;
    }

    public static int getOwner() {
        return ownerId;
    }

    public static int getGameId() {
        return gameId;
    }

    public static List<Integer> getPlayersInGame() {
        return playersInGame;
    }

    public static void setPlayersInGame(List<Integer> players) {
        playersInGame = players;
    }

    public static void addPlayerToGame(int playerId) {
        playersInGame.add(playerId);
    }

    public static void setCanBreakWall(boolean bool){
        canBreakWall=bool;
    }

    public static boolean getCanBreakWall(){
        return canBreakWall
    }
}
