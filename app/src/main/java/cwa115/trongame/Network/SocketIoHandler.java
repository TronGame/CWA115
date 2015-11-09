package cwa115.trongame.Network;

import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public interface SocketIoHandler {
    void onRemoteLocationChange(String playerId, String playerName, LatLng location);
    void onRemoteWallUpdate(String playerId, String wallId, LatLng points);
}
