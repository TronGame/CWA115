package cwa115.trongame.Network;

import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public interface SocketIoHandler {
    public void onRemoteLocationChange(LatLng location);
}
