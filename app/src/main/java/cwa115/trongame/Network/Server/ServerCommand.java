package cwa115.trongame.Network.Server;

/**
 * Easy way to store all available server commands.
 */
public enum ServerCommand {
    // name => (string) New player's name ; pictureUrl => (string) Url to player's profile picture ;
    // friends => (Array []) The player's friends' userIds ; tokenLength => (integer) length of
    // token to be created (default=25)
    // Returns: id => (int) The player's id ; token => (string) The player's token
    INSERT_ACCOUNT("insertAccount"),

    // id => (integer) Player's id ; token => (integer) Player's token
    // Returns: id => (integer) Player's id ; name => (string) Player's name ; pictureUrl => (string)
    // Url to player's profile picture ; friends => (Array []) Player's friends' userIds
    SHOW_ACCOUNT("showAccount"),

    // id => (integer) Player's id ; token => (integer) Player's token ; params => see insertAccount
    // for available parameters
    // Returns: success => (boolean) True when update was successful, False otherwise
    UPDATE_ACCOUNT("updateAccount"),

    // id => (integer) Player's id ; token => (integer) Player's token
    // Returns: success => (boolean) True when deletion was successful, False otherwise
    DELETE_ACCOUNT("deleteAccount"),

    GET_FRIEND_IDS("getFriendIds"),

    // name => (string) Room name ; token => (integer) Owner's token ; owner => (integer) userId of owner ; maxPlayers =>
    // (integer) Maximum number of players ; tokenLength => (integer) length of token to be created (default=25)
    // Returns: token => (string) Game's token ; id => (integer) Game's id
    INSERT_GAME("insertGame"),

    SHOW_GAME("showGame"),

    // id => (integer) Room id OR name => (string) Room name ; token => (integer) Room token
    // Returns: nothing
    DELETE_GAME("deleteGame"),

    // gameId => (integer) Room id ; id => (integer) Player id ; token => (integer) Player token
    // Returns: nothing
    START_GAME("startGame"),

    JOIN_GAME("joinGame"),

    LEAVE_GAME("leaveGame"),

    LIST_GAMES("listGames"),

    KICK_PLAYER("kickPlayer"),

    SCOREBOARD("scoreboard"),

    INCREASE_WINS("increaseWins"),

    INCREASE_LOSSES("increaseLosses"),

    INCREASE_COMMON_PLAYS("increaseCommonPlays"),

    SET_HIGHSCORE("setHighscore"),

    SET_PLAYTIME("setPlaytime"),

    ADD_FRIENDS("addFriends"),

    DELETE_FRIEND("deleteFriend"),

    ACCEPT_FRIEND("acceptFriend"),

    ADD_INVITE("addInvite"),

    DELETE_INVITE("deleteInvite"),

    SHOW_INVITES("showInvites"),

    END_GAME("endGame");

    private final String command;
    ServerCommand(String command) { this.command = command; }
    public String getValue() { return command; }
}
