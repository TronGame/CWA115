package cwa115.trongame;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
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
    // Server parameter names
    public final static String SERVER_ID_PARAM = "id";
    public final static String SERVER_TOKEN_PARAM = "token";
    public final static String SERVER_NAME_PARAM = "name";
    public final static String SERVER_PICTURE_URL_PARAM = "pictureUrl";
    public final static String SERVER_FRIENDS_PARAM = "friends";
    public final static String SERVER_FACEBOOK_ID_PARAM = "facebookId";

    private Integer Id;//, Wins, Losses, Highscore, Playtime;
    private Long FacebookId;
    private String Token, Name, PictureUrl;
    private FriendList Friends;

    public Profile(){
        this(null, null, null, null, null, null);
    }
    public Profile(String Name){
        this(null, null, null, Name, null, null);
    }
    public Profile(String Name, String PictureUrl, JSONArray Friends){
        this(null, null, null, Name, PictureUrl, new FriendList(Friends));
    }
    public Profile(String Name, String PictureUrl, Long[] Friends){
        this(null, null, null, Name, PictureUrl, new FriendList(Arrays.asList(Friends)));
    }
    public Profile(Integer Id, String Token, String Name, String PictureUrl, JSONArray Friends){
        this(Id, Token, null, Name, PictureUrl, new FriendList(Friends));
    }
    public Profile(Integer Id, String Token, Long FacebookId, String Name, String PictureUrl, FriendList Friends){
        this.Id = Id;
        this.Token = Token;
        this.Name = Name;
        this.PictureUrl = PictureUrl;
        this.Friends = Friends;
        this.FacebookId = FacebookId;
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
            query.put(SERVER_FRIENDS_PARAM, this.Friends.toString());
        if (this.FacebookId != null && params.contains(SERVER_FACEBOOK_ID_PARAM))
            query.put(SERVER_FACEBOOK_ID_PARAM, String.valueOf(this.FacebookId));
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
                SERVER_FRIENDS_PARAM
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
        return new Profile(Id, Token, FacebookId, Name, PictureUrl, Friends);
    }

    public static void Delete(SharedPreferences settings){
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ACCOUNT_ID_KEY);
        editor.remove(ACCOUNT_TOKEN_KEY);
        editor.remove(ACCOUNT_FACEBOOK_ID_KEY);
        editor.remove(ACCOUNT_NAME_KEY);
        editor.remove(ACCOUNT_PICTURE_URL_KEY);
        editor.remove(ACCOUNT_FRIENDS_KEY);
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
            newFriendIds.retainAll(oldFriendIds);
            updatedData.Friends = new FriendList(newFriendIds);
        }
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
    //endregion

    //region Getters
    public Integer getId(){ return this.Id; }
    public String getToken(){ return this.Token; }
    public Long getFacebookId(){ return this.FacebookId; }
    public String getName(){ return this.Name; }
    public String getPictureUrl(){ return this.PictureUrl; }
    public FriendList getFriends(){ return this.Friends; }
    //endregion

    //region Parcelling part
    public Profile(Parcel in){
        String[] strings = new String[4];
        int id = in.readInt();
        long facebookId = in.readLong();
        in.readStringArray(strings);

        this.Id = (id==-1) ? null : id;
        this.FacebookId = (facebookId==-1) ? null : facebookId;
        this.Token = strings[0];
        this.Name = strings[1];
        this.PictureUrl = strings[2];
        try {
            this.Friends = new FriendList(strings[3]);
        }catch(JSONException e){
            e.printStackTrace();
            this.Friends = null;
        }
    }

    public int describeContents(){
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Id);
        dest.writeLong(this.FacebookId);
        String f = this.Friends.toString();
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
}
