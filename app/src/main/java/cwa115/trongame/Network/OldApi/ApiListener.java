package cwa115.trongame.Network.OldApi;

/**
 * Created by Peter on 09/11/2015.
 */
public interface ApiListener<T> {
    public boolean handleApiResult(T result);
}
