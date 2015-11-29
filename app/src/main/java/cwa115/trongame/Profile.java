package cwa115.trongame;

import android.content.SharedPreferences;

import org.json.JSONArray;

/**
 * Created by Bram on 28-11-2015.
 */
public class Profile {

    private final static String ACCOUNT_ID_KEY = "accountId";
    private final static String ACCOUNT_TOKEN_KEY = "accountToken";
    private final static String ACCOUNT_NAME_KEY = "accountName";
    private final static String ACCOUNT_PICTURE_URL = "accountPictureUrl";
    private final static String ACCOUNT_FRIENDS = "accountFriends";

    private Integer Id;
    private String Token, Name, PictureUrl;
    private JSONArray Friends;

    public Profile(){
        this(null, null, null, null, null);
    }
    public Profile(String Name, String PictureUrl, JSONArray Friends){
        this(null, null, Name, PictureUrl, Friends);
    }
    public Profile(Integer Id, String Token, String Name, String PictureUrl, JSONArray Friends){
        this.Id = Id;
        this.Token = Token;
        this.Name = Name;
        this.PictureUrl = PictureUrl;
        this.Friends = Friends;
    }

    public void Store(SharedPreferences settings){
        SharedPreferences.Editor editor = settings.edit();
        if(this.Id!=null)
            editor.putInt(ACCOUNT_ID_KEY, this.Id);
        if(this.Token!=null)
            editor.putString(ACCOUNT_TOKEN_KEY, this.Token);
        if(this.Name!=null)
            editor.putString(ACCOUNT_NAME_KEY, this.Name);
        if(this.PictureUrl!=null)
            editor.putString(ACCOUNT_PICTURE_URL, this.PictureUrl);
        if(this.Friends!=null)
            editor.putString(ACCOUNT_FRIENDS, this.Friends.toString());
        editor.apply();
    }

    //region Setters
    public void setId(Integer Id){ this.Id = Id; }
    public void setToken(String Token){ this.Token = Token; }
    public void setName(String Name){ this.Name = Name; }
    public void setPictureUrl(String PictureUrl){ this.PictureUrl = PictureUrl; }
    public void setFriends(JSONArray Friends){ this.Friends = Friends; }
    //endregion

    //region Getters
    public Integer getId(){ return this.Id; }
    public String getToken(){ return this.Token; }
    public String getName(){ return this.Name; }
    public String getPictureUrl(){ return this.PictureUrl; }
    public JSONArray getFriends(){ return this.Friends; }
    //endregion
}
