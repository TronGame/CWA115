package cwa115.trongame.Lists;

import cwa115.trongame.User.Profile;

public class ScoreListItem {
    private Profile player;
    private int gamesWon;

    public ScoreListItem(int id, int wins, String name, String pictureUrl) {
        gamesWon = wins;
        player = new Profile(id, null, name, pictureUrl, new Long[0]);
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public String getPlayerPictureUrl(){
        return player.getPictureUrl();
    }

    public Profile getProfile(){
        return player;
    }
}

