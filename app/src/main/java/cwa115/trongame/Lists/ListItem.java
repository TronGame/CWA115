package cwa115.trongame.Lists;

public class ListItem {
    private String gamename;
    private String host;
    private String players;

    // Constructor for the ListItem class
    public ListItem(String name, String host, String players) {
        super();
        this.gamename = name;
        this.host = host;
        this.players = players;
    }

    // Getter and setter methods for all the fields.
    // Though you would not be using the setters for this example,
    // it might be useful later.
    public String getGamename() {
        return gamename;
    }
    public void setgamename(String gamename) {
        this.gamename = gamename;
    }
    public String getHost() {
        return host;
    }
    public void sethost(String host) {
        this.host = host;
    }
    public String getPlayers() {
        return players;
    }
    public void setplayers(String players) {
        this.players = players;
    }
}
