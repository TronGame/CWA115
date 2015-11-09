package cwa115.trongame.Network;

import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public interface SocketIoHandler {
    void onPlayerJoined(String playerId, String playerName);
    void onRemoteLocationChange(String playerId, LatLng location);
    void onRemoteWallUpdate(String playerId, String wallId, LatLng points);
}
