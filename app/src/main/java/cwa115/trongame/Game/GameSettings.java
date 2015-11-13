package cwa115.trongame.Game;

import java.util.UUID;

/**
 * Singleton to manage the settings of the current game.
 * These settings are not persistent.
 */
public final class GameSettings {

    private static String playerName;
    private static int playerMarkerImage;
    private static int wallColor;
    private static String token;

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
     * (Randomly) generates a unique ID.
     * @return the generated ID
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static void setToken(String tk) {
        token = tk;
    }
}
