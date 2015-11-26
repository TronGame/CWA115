package cwa115.trongame.Lists;

public class RoomListItem {
    private String playerName;
    private int color;
    private int id;


    // Constructor for the LobbyListItem class
    public RoomListItem(String name, int color, int id) {
        super();
        this.playerName = name;
        this.color =color;
        this.id = id;
    }

    // Getter and setter methods for all the fields.
    // Though you would not be using the setters for this example,
    // it might be useful later.
    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public int getPlayerId(){
        return id;
    }
    public void setPlayerId(int playerId){
        id = playerId;
    }

}
