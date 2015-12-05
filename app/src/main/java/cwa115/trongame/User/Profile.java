package cwa115.trongame.User;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bram on 28-11-2015.
 */
public class Profile implements Parcelable{

    // Local device storage keys
    private final static String ACCOUNT_ID_KEY = "accountId";
    private final static String ACCOUNT_TOKEN_KEY = "accountToken";
    private final static String ACCOUNT_NAME_KEY = "accountName";
    private final static String ACCOUNT_PICTURE_URL_KEY = "accountPictureUrl";
    private final static String ACCOUNT_FRIENDS_KEY = "accountFriends";
    private final static String ACCOUNT_FACEBOOK_ID_KEY = "accountFacebookId";
    private final static String ACCOUNT_WINS_KEY = "accountWins";
    private final static String ACCOUNT_LOSSES_KEY = "accountLosses";
    private final static String ACCOUNT_HIGHSCORE_KEY = "accountHighscore";
    private final static String ACCOUNT_PLAYTIME_KEY = "accountPlaytime";
    // Server parameter names
    public final static String SERVER_ID_PARAM = "id";
    public final static String SERVER_TOKEN_PARAM = "token";
    public final static String SERVER_NAME_PARAM = "name";
    public final static String SERVER_PICTURE_URL_PARAM = "pictureUrl";
    public final static String SERVER_FRIENDS_PARAM = "friends";
    public final static String SERVER_FRIEND_PARAM = "friendId";
    public final static String SERVER_FACEBOOK_ID_PARAM = "facebookId";
    //public final static String SERVER_WINS_PARAM = "wins";
    //public final static String SERVER_LOSSES_PARAM = "losses";
    public final static String SERVER_HIGHSCORE_PARAM = "highscore";
    public final static String SERVER_PLAYTIME_PARAM = "playtime";

    private Integer Id, Wins, Losses, Highscore, Playtime;
    private Long FacebookId;
    private String Token, Name, PictureUrl;
    private FriendList Friends;

    public Profile(){
        this(null, null, null, null, null, null, null, null, null, null);
    }
    public Profile(String Name){
        this(null, null, null, Name, null, null, null, null, null, null);
    }
    public Profile(String Name, String PictureUrl, JSONArray Friends){
        this(null, null, null, Name, PictureUrl, null, null, null, null, new FriendList(Friends));
    }
    public Profile(String Name, String PictureUrl, Long[] Friends){
        this(null, null, null, Name, PictureUrl, null, null, null, null, new FriendList(Arrays.asList(Friends)));
    }
    public Profile(Integer Id, String Token, String Name, String PictureUrl, JSONArray Friends){
        this(Id, Token, null, Name, PictureUrl, null, null, null, null, new FriendList(Friends));
    }
    public Profile(Integer Id, String Token, Long FacebookId, String Name, String PictureUrl,
                   Integer Wins, Integer Losses, Integer Highscore, Integer Playtime, FriendList Friends){
        this.Id = Id;
        this.Token = Token;
        this.Name = Name;
        this.PictureUrl = PictureUrl;
        this.Friends = Friends;
        this.FacebookId = FacebookId;
        this.Wins = Wins;
        this.Losses = Losses;
        this.Highscore = Highscore;
        this.Playtime = Playtime;
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
            editor.putString(ACCOUNT_PICTURE_URL_KEY, this.PictureUrl);
        if(this.Friends!=null)
            editor.putString(ACCOUNT_FRIENDS_KEY, this.Friends.toString());
        if(this.FacebookId!=null)
            editor.putLong(ACCOUNT_FACEBOOK_ID_KEY, this.FacebookId);
        if(this.Wins!=null)
            editor.putInt(ACCOUNT_WINS_KEY, this.Wins);
        if(this.Losses!=null)
            editor.putInt(ACCOUNT_LOSSES_KEY, this.Losses);
        if(this.Highscore!=null)
            editor.putInt(ACCOUNT_HIGHSCORE_KEY, this.Highscore);
        if(this.Playtime!=null)
            editor.putInt(ACCOUNT_PLAYTIME_KEY, this.Playtime);
        editor.apply();
    }

    public Map<String, String> GetQuery(List<String> params) {
        Map<String, String> query = new HashMap<>();
        if(this.Id!=null && params.contains(SERVER_ID_PARAM))
            query.put(SERVER_ID_PARAM, String.valueOf(this.Id));
        if(this.Token!=null && params.contains(SERVER_TOKEN_PARAM))
            query.put(SERVER_TOKEN_PARAM, this.Token);
        if(this.Name!=null && params.contains(SERVER_NAME_PARAM))
            query.put(SERVER_NAME_PARAM, this.Name);
        if(this.PictureUrl!=null && params.contains(SERVER_PICTURE_URL_PARAM))
            query.put(SERVER_PICTURE_URL_PARAM, this.PictureUrl);
        if (this.Friends != null && params.contains(SERVER_FRIENDS_PARAM))
            query.put(SERVER_FRIENDS_PARAM, new JSONArray(this.Friends.ToIdList()).toString());
        if (this.FacebookId != null && params.contains(SERVER_FACEBOOK_ID_PARAM))
            query.put(SERVER_FACEBOOK_ID_PARAM, String.valueOf(this.FacebookId));
        if (this.Friends != null && this.Friends.size() > 0 && params.contains(SERVER_FRIEND_PARAM))
            query.put(SERVER_FRIEND_PARAM, this.Friends.ToIdList().get(0).toString());
        if (this.Highscore != null && params.contains(SERVER_HIGHSCORE_PARAM))
            query.put(SERVER_HIGHSCORE_PARAM, String.valueOf(this.Highscore));
        if (this.Playtime != null && params.contains(SERVER_PLAYTIME_PARAM))
            query.put(SERVER_PLAYTIME_PARAM, String.valueOf(this.Playtime));
        return query;
    }
    public Map<String, String> GetQuery(String... params){
        return GetQuery(Arrays.asList(params));
    }
    public Map<String, String> GetQuery(){
        return GetQuery(Lists.newArrayList(
                SERVER_ID_PARAM,
                SERVER_TOKEN_PARAM,
                SERVER_FACEBOOK_ID_PARAM,
                SERVER_NAME_PARAM,
                SERVER_PICTURE_URL_PARAM,
                SERVER_FRIENDS_PARAM,
                SERVER_FRIEND_PARAM,
                SERVER_HIGHSCORE_PARAM,
                SERVER_PLAYTIME_PARAM
        ));
    }

    //region Static Methods
    public static Profile Load(SharedPreferences settings){
        int id = settings.getInt(ACCOUNT_ID_KEY, -1);
        Integer Id = (id==-1) ? null : id;
        String Token = settings.getString(ACCOUNT_TOKEN_KEY, null);
        String Name = settings.getString(ACCOUNT_NAME_KEY, null);
        String PictureUrl = settings.getString(ACCOUNT_PICTURE_URL_KEY, null);
        String friends = settings.getString(ACCOUNT_FRIENDS_KEY, null);
        FriendList Friends = null;
        try{
            Friends = (friends==null) ? null : new FriendList(friends);
        }catch(JSONException e){
            e.printStackTrace();
        }
        long facebookId = settings.getLong(ACCOUNT_FACEBOOK_ID_KEY, -1);
        Long FacebookId = (facebookId==-1) ? null : facebookId;
        int wins = settings.getInt(ACCOUNT_WINS_KEY, -1);
        int losses = settings.getInt(ACCOUNT_LOSSES_KEY, -1);
        int highscore = settings.getInt(ACCOUNT_HIGHSCORE_KEY, -1);
        int playtime = settings.getInt(ACCOUNT_PLAYTIME_KEY, -1);
        Integer Wins = (wins==-1) ? null : wins;
        Integer Losses = (losses==-1) ? null : losses;
        Integer Highscore = (highscore==-1) ? null : highscore;
        Integer Playtime = (playtime==-1) ? null : playtime;
        return new Profile(Id, Token, FacebookId, Name, PictureUrl, Wins, Losses, Highscore, Playtime, Friends);
    }

    public static void Delete(SharedPreferences settings){
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ACCOUNT_ID_KEY);
        editor.remove(ACCOUNT_TOKEN_KEY);
        editor.remove(ACCOUNT_FACEBOOK_ID_KEY);
        editor.remove(ACCOUNT_NAME_KEY);
        editor.remove(ACCOUNT_PICTURE_URL_KEY);
        editor.remove(ACCOUNT_FRIENDS_KEY);
        editor.remove(ACCOUNT_WINS_KEY);
        editor.remove(ACCOUNT_LOSSES_KEY);
        editor.remove(ACCOUNT_HIGHSCORE_KEY);
        editor.remove(ACCOUNT_PLAYTIME_KEY);
        editor.apply();
    }

    public static Profile GetUpdatedData(Profile oldProfile, Profile newProfile){
        Profile updatedData = new Profile();
        if(newProfile.Id!=null && !newProfile.Id.equals(oldProfile.Id))
            updatedData.Id = newProfile.Id;
        if(newProfile.Token!=null && !newProfile.Token.equals(oldProfile.Token))
            updatedData.Token = newProfile.Token;
        if(newProfile.FacebookId!=null && !newProfile.FacebookId.equals(oldProfile.FacebookId))
            updatedData.FacebookId = newProfile.FacebookId;
        if(newProfile.Name!=null && !newProfile.Name.equals(oldProfile.Name))
            updatedData.Name = newProfile.Name;
        if(newProfile.PictureUrl!=null && !newProfile.PictureUrl.equals(oldProfile.PictureUrl))
            updatedData.PictureUrl = newProfile.PictureUrl;
        if(newProfile.Friends!=null && !newProfile.Friends.equals(oldProfile.Friends)) {
            List<Long> oldFriendIds = oldProfile.Friends.ToIdList();
            List<Long> newFriendIds = newProfile.Friends.ToIdList();
            newFriendIds.removeAll(oldFriendIds);
            updatedData.Friends = new FriendList(newFriendIds);
        }
        if(newProfile.Wins!=null && newProfile.Wins.equals(oldProfile.Wins))
            updatedData.Wins = newProfile.Wins;
        if(newProfile.Losses!=null && newProfile.Losses.equals(oldProfile.Losses))
            updatedData.Losses = newProfile.Losses;
        if(newProfile.Highscore!=null && newProfile.Highscore.equals(oldProfile.Highscore))
            updatedData.Highscore = newProfile.Highscore;
        if(newProfile.Playtime!=null && newProfile.Playtime.equals(oldProfile.Playtime))
            updatedData.Playtime = newProfile.Playtime;
        return updatedData;
    }
    //endregion

    //region Setters
    public void setId(Integer Id){ this.Id = Id; }
    public void setToken(String Token){ this.Token = Token; }
    public void setFacebookId(Long FacebookId){ this.FacebookId = FacebookId; }
    public void setName(String Name){ this.Name = Name; }
    public void setPictureUrl(String PictureUrl){ this.PictureUrl = PictureUrl; }
    public void setFriends(FriendList Friends){ this.Friends = Friends; }
    public void setWins(Integer Wins){ this.Wins = Wins; }
    public void setLosses(Integer Losses){ this.Losses = Losses; }
    public void setHighscore(Integer Highscore){ this.Highscore = Highscore; }
    public void setPlaytime(Integer Playtime){ this.Playtime = Playtime; }
    //endregion

    //region Getters
    public Integer getId(){ return this.Id; }
    public String getToken(){ return this.Token; }
    public Long getFacebookId(){ return this.FacebookId; }
    public String getName(){ return this.Name; }
    public String getPictureUrl(){ return this.PictureUrl; }
    public FriendList getFriends(){ return this.Friends; }
    public Integer getWins(){ return this.Wins; }
    public Integer getLosses(){ return this.Losses; }
    public Integer getHighscore(){ return this.Highscore; }
    public Integer getPlaytime(){ return this.Playtime; }
    //endregion

    //region Parcelling part
    public Profile(Parcel in){
        String[] strings = new String[4];
        int[] ints = new int[5];
        in.readIntArray(ints);
        long facebookId = in.readLong();
        in.readStringArray(strings);

        this.Id = fromInt(ints[0]);
        this.FacebookId = fromLong(facebookId);
        this.Token = strings[0];
        this.Name = strings[1];
        this.PictureUrl = strings[2];
        try {
            this.Friends = new FriendList(strings[3]);
        }catch(JSONException e){
            e.printStackTrace();
            this.Friends = null;
        }
        this.Wins = fromInt(ints[1]);
        this.Losses = fromInt(ints[2]);
        this.Highscore = fromInt(ints[3]);
        this.Playtime = fromInt(ints[4]);
    }

    public int describeContents(){
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{
                toInt(this.Id),
                toInt(this.Wins),
                toInt(this.Losses),
                toInt(this.Highscore),
                toInt(this.Playtime)
        });
        dest.writeLong(toLong(this.FacebookId));
        String f = (Friends == null) ? "" : this.Friends.toString();
        dest.writeStringArray(new String[]{
                this.Token,
                this.Name,
                this.PictureUrl,
                f
        });
    }
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
    //endregion

    //region Utilities
    private int toInt(Integer i){
        return i==null ? -1 : i;
    }
    private Integer fromInt(int i){
        return i==-1 ? null : i;
    }
    private long toLong(Long l){
        return l==null ? -1 : l;
    }
    private Long fromLong(long l){
        return l==-1 ? null : l;
    }
    //endregion
}
