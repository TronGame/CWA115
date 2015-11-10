package cwa115.trongame.Network;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 *
 */
public interface SocketIoHandler {
    public boolean onMessage(JSONObject message);
}
