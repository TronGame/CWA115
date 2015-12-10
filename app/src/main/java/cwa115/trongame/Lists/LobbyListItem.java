package cwa115.trongame.Lists;

public class LobbyListItem {
    private String gamename;
    private String host;
    private int hostId;
    private int players;
    private boolean canBreakWalls;
    private int timeLimit;
    private double maxDist;
    private int playerCount;

    // Constructor for the LobbyListItem class
    public LobbyListItem(String name, String host, int hostId, int maxPlayers, int playerCount, boolean canBreakWalls, int timeLimit, double maxDist) {
        super();
        this.gamename = name;
        this.host = host;
        this.hostId = hostId;
        this.canBreakWalls = canBreakWalls;
        this.timeLimit = timeLimit;
        this.playerCount = playerCount;
        this.players = maxPlayers;
        this.maxDist = maxDist;
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
    public int getHostId() {
        return hostId;
    }
    public void setHostId(int hostId) {
        this.hostId = hostId;
    }
    public int getMaxPlayersAsInteger() {
        return players;
    }
    public String getMaxPlayers() {
        return Integer.toString(players);
    }
    public void setMaxPlayers(int players) {
        this.players = players;
    }
    public boolean getCanBreakWall() {return this.canBreakWalls;}
    public int getTimeLimit() {
        return timeLimit;
    }
    public double getMaxDist() {
        return maxDist;
    }
    public int getPlayerCount() { return playerCount; }
}
