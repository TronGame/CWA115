package cwa115.trongame;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bram on 2-12-2015.
 */
public class Friend {

    private long Id;
    private int CommonPlays;
    private boolean Pending, Inviter;

    public Friend(long Id){
        this(Id, true, true, 0);
    }
    public Friend(long Id, boolean Pending, boolean Inviter, int CommonPlays){
        this.Id = Id;
        this.Pending = Pending;
        this.Inviter = Inviter;
        this.CommonPlays = CommonPlays;
    }
    public Friend(JSONObject friendJSON){
        this(0, true, true, 0);
        try {
            this.Id = friendJSON.has("id") ? friendJSON.getLong("id") : 0;
            if (!friendJSON.has("accepted") && !friendJSON.has("pending")) {
                this.Pending = true;
            } else if (!friendJSON.has("accepted")) {
                this.Inviter = false;
                this.Pending = friendJSON.getInt("pending") == 1;
            } else {
                this.Pending = friendJSON.getInt("accepted") == 0;
            }
            this.CommonPlays = friendJSON.has("commonPlays") ? friendJSON.getInt("commonPlays") : 0;
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    
    public JSONObject ToJSONObject(){
        JSONObject data = new JSONObject();
        try {
            data.put("id", this.Id);
            if(this.Inviter)
                data.put("accepted",this.Pending ? 0 : 1);
            else
                data.put("pending",this.Pending ? 1 : 0);
            data.put("commonPlays",this.CommonPlays);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return data;
    }

    public boolean isPending(){
        return this.Pending;
    }
    public boolean isInviter(){
        return this.Inviter;
    }
    public long getId(){
        return this.Id;
    }
    public int getCommonPlays(){
        return this.CommonPlays;
    }
/*
    //region Parcelable Methods
    public Friend(Parcel in){
        int[] intData = new int[3];
        in.readIntArray(intData);

        this.Id = in.readLong();
        this.CommonPlays = intData[0];
        this.Pending = intData[1]==1;
        this.Inviter = intData[2]==1;
    }

    public int describeContents(){
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.Id);
        dest.writeIntArray(new int[]{this.CommonPlays, this.Pending ? 1 : 0, this.Inviter ? 1 : 0});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
    //endregion*/
}
