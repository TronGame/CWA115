package cwa115.trongame;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Bram on 2-12-2015.
 */
public class FriendList implements List<Friend> {

    private List<Friend> list;

    public FriendList(){
        this.list = new ArrayList<>();
    }
    public FriendList(JSONArray friendsJSON) {
        this();
        try {
            for (int i = 0; i < friendsJSON.length(); i++) {
                JSONObject friendData = new JSONObject(friendsJSON.getString(i));
                this.list.add(new Friend(friendData));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public FriendList(List<Long> ids){
        this();
        for(long id : ids)
            this.list.add(new Friend(id));
    }
    public FriendList(String friendsJSONString) throws JSONException{
        this(new JSONArray(friendsJSONString));
    }

    public JSONArray ToJSONArray(){
        JSONArray data = new JSONArray();
        for(Friend friend : this.list){
            data.put(friend.ToJSONObject());
        }
        return data;
    }
    public List<Long> ToIdList(){
        return Lists.transform(this.list, new Function<Friend, Long>() {
            @Override
            public Long apply(Friend input) {
                return input.getId();
            }
        });
    }

    @Override
    public String toString(){
        return this.ToJSONArray().toString();
    }

    @Override
    public void add(int location, Friend object) {
        list.add(location, object);
    }

    @Override
    public boolean add(Friend object) {
        return list.add(object);
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends Friend> collection) {
        return list.addAll(location, collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Friend> collection) {
        return list.addAll(collection);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean contains(Object object) {
        return list.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return list.containsAll(collection);
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof FriendList))
            return false;
        List<Friend> o = (FriendList)object;
        return list.equals(o);
    }

    @Override
    public Friend get(int location) {
        return list.get(location);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public int indexOf(Object object) {
        return list.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<Friend> iterator() {
        return list.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return list.lastIndexOf(object);
    }

    @Override
    public ListIterator<Friend> listIterator() {
        return list.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<Friend> listIterator(int location) {
        return list.listIterator(location);
    }

    @Override
    public Friend remove(int location) {
        return list.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return list.remove(object);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return list.retainAll(collection);
    }

    @Override
    public Friend set(int location, Friend object) {
        return list.set(location, object);
    }

    @Override
    public int size() {
        return list.size();
    }

    @NonNull
    @Override
    public List<Friend> subList(int start, int end) {
        return list.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        return list.toArray(array);
    }
/*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FriendList createFromParcel(Parcel in) {
            return new FriendList(in);
        }

        public FriendList[] newArray(int size) {
            return new FriendList[size];
        }
    };

    public FriendList(Parcel in){
        this();
    }*/
}
