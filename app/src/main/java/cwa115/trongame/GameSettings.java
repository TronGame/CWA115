package cwa115.trongame;

import java.util.UUID;

/**
 * Singleton to manage the settings of the current game.
 * These settings are not persistent.
 */
public final class GameSettings {

    private static String playerName;
    private static int playerMarkerImage;

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
     * (Randomly) generates a unique ID.
     * @return the generated ID
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
