package cwa115.trongame.Lists;

public class ScoreListItem {
    private String playerName;
    private int gamesWon;

    public ScoreListItem(String name, int wins) {
        playerName = name;
        gamesWon = wins;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getGamesWon() {
        return gamesWon;
    }
}
