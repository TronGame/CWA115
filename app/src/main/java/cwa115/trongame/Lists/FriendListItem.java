package cwa115.trongame.Lists;

public class FriendListItem {
    private String playerName;
    private long playerId;
    private boolean selected;


    // Constructor for the LobbyListItem class
    public FriendListItem(long id, String name, boolean selected) {
        super();
        this.playerName = name;
        this.playerId = id;
        this.selected = selected;
    }
    public FriendListItem(long id, String name){
        this(id, name, false);
    }

    public String getPlayerName() {
        return playerName;
    }
    public long getPlayerId(){
        return playerId;
    }
    public boolean isSelected(){
        return selected;
    }
    public void setSelected(boolean state){
        selected = state;
    }

}
