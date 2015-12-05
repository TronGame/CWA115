package cwa115.trongame.Game;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cwa115.trongame.User.FriendList;
import cwa115.trongame.User.Profile;

/**
 * Singleton to manage the settings of the current game.
 * These settings are not persistent.
 */
public final class GameSettings {
    private static Profile profile;
    private static ArrayList<Integer> playersInGame;

    private static int playerMarkerImage;
    private static int wallColor;
    private static int ownerId;
    private static int gameId;
    private static int maxPlayers;
    private static int timeLimit = -1;

    private static long lastPlayTime = -1;

    private static boolean spectate = false;
    private static boolean canBreakWall;

    private static String gameToken;
    private static String gameName;

    public static void storeInBundle(Bundle bundle) {
        bundle.putParcelable("profile", profile);
        bundle.putIntegerArrayList("playersInGame", playersInGame);

        bundle.putInt("playerMarkerImage", playerMarkerImage);
        bundle.putInt("wallColor", wallColor);
        bundle.putInt("ownerId", ownerId);
        bundle.putInt("gameId", gameId);
        bundle.putInt("maxPlayers", maxPlayers);
        bundle.putInt("timeLimit", timeLimit);

        bundle.putLong("lastPlayTime", lastPlayTime);

        bundle.putBoolean("spectate", spectate);
        bundle.putBoolean("spectate", canBreakWall);

        bundle.putString("gameToken", gameToken);
        bundle.putString("gameName", gameName);
    }

    public static void loadFromBundle(Bundle bundle) {
        profile = bundle.getParcelable("profile");
        playersInGame = bundle.getIntegerArrayList("playersInGame");

        playerMarkerImage = bundle.getInt("playerMarkerImage", playerMarkerImage);
        wallColor = bundle.getInt("wallColor", wallColor);
        ownerId = bundle.getInt("ownerId", ownerId);
        gameId = bundle.getInt("gameId", gameId);
        maxPlayers = bundle.getInt("maxPlayers", maxPlayers);
        timeLimit = bundle.getInt("timeLimit", timeLimit);

        lastPlayTime  = bundle.getLong("lastPlayTime", lastPlayTime);

        spectate = bundle.getBoolean("spectate", spectate);
        canBreakWall = bundle.getBoolean("spectate", canBreakWall);

        gameToken = bundle.getString("gameToken", gameToken);
        gameName = bundle.getString("gameName", gameName);
    }

    /**
     * Sets the profile of the player.
     * @param p the profile of the player
     */
    public static void setProfile(Profile p){ profile = p; }

    /**
     * @return the player's profile
     */
    public static Profile getProfile(){ return profile; }

    /**
     * Sets the name of the local player.
     * @param name the name of the player
     */
    public static void setPlayerName(String name) {
        profile.setName(name);
    }

    /**
     * @return the name of the (local) player
     */
    public static String getPlayerName() {
        return profile.getName();
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
        profile.setToken(token);
    }

    public static void setUserId(int id) {
        profile.setId(id);
    }

    public static int getUserId() {
        return profile.getId();
    }

    public static String getPlayerId() {return String.valueOf(getUserId());}

    public static void setGameId(int gameId) {
        GameSettings.gameId = gameId;
    }

    public static void setGameToken(String gameToken) {
        GameSettings.gameToken = gameToken;
    }

    public static String getGameToken() {
        return gameToken;
    }

    public static void setGameName(String gameName) {
        GameSettings.gameName = gameName;
    }

    public static String getGameName(){
        return gameName;
    }

    public static String getPlayerToken() {
        return profile.getToken();
    }

    /**
     * @return Is this player the owner?
     */
    public static boolean isOwner() {
        return (ownerId==getUserId()) && gameToken != null;
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

    public static void setPlayersInGame(ArrayList<Integer> players) {
        playersInGame = players;
    }

    public static void addPlayerToGame(int playerId) {
        playersInGame.add(playerId);
    }

    public static void setFriends(FriendList playerFriends){ profile.setFriends(playerFriends); }

    public static FriendList getFriends(){ return profile.getFriends(); }

    public static void setCanBreakWall(boolean bool){
        canBreakWall=bool;
    }
    public static boolean getCanBreakWall(){
        return canBreakWall;
    }

    public static void setTimeLimit(int minutes){
        timeLimit =minutes;
    }
    public static int getTimeLimit(){
        return timeLimit;
    }

    public static void setSpectate(boolean bool){
        spectate=bool;
    }
    public static boolean getSpectate(){
        return spectate;
    }

    public static void resetLastPlaytime(){ lastPlayTime = -1; }
    public static void setLastPlayTime(long playTime){ lastPlayTime = playTime; }
    public static long getLastPlayTime(){ return lastPlayTime; }
}

