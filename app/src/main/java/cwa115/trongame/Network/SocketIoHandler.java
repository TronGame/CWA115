package cwa115.trongame.Network;

import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public interface SocketIoHandler {
    public void onPlayerJoined(String playerId, String playerName);
    public void onRemoteLocationChange(String playerId, LatLng location);
}
