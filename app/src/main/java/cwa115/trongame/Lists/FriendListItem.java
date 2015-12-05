package cwa115.trongame.Lists;

import cwa115.trongame.User.Friend;
import cwa115.trongame.User.Profile;

public class FriendListItem {
    private Friend friend;
    private Profile friendProfile;
    private boolean selected;


    // Constructor for the LobbyListItem class
    public FriendListItem(Friend friend, Profile friendProfile, boolean selected) {
        super();
        this.friend = friend;
        this.friendProfile = friendProfile;
        this.selected = selected;
    }
    public FriendListItem(Friend friend, Profile friendProfile){
        this(friend, friendProfile, false);
    }

    public Friend getPlayer() {
        return friend;
    }
    public Profile getPlayerProfile(){ return friendProfile; }
    public boolean isSelected(){
        return selected;
    }
    public void setSelected(boolean state){
        selected = state;
    }

}
