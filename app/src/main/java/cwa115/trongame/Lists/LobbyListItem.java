package cwa115.trongame.Lists;

public class LobbyListItem {
    private String gamename;
    private String host;
    private int players;

    // Constructor for the LobbyListItem class
    public LobbyListItem(String name, String host, int maxPlayers) {
        super();
        this.gamename = name;
        this.host = host;
        this.players = maxPlayers;
    }

    // Getter and setter methods for all the fields.
    // Though you would not be using the setters for this example,
    // it might be useful later.
    public String getGamename() {
        return gamename;
    }
    public void setGamename(String gamename) {
        this.gamename = gamename;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPlayersAsInteger() {
        return players;
    }
    public String getPlayers() {
        return Integer.toString(players);
    }
    public void setPlayers(int players) {
        this.players = players;
    }


}
