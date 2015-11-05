package cwa115.trongame;

/**
 * Singleton to manage the settings of the current game.
 * These settings are not persistent.
 */
public final class GameSettings {

    private static String playerName;
    private static int playerMarkerImage;

    public static void setPlayerName(String name) {
        playerName = name;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerMarkerImage(int markerId) {
        playerMarkerImage = markerId;
    }

    public static int getPlayerMarkerImage() {
        return playerMarkerImage;
    }
}
