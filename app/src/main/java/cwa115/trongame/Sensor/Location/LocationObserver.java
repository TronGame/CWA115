package cwa115.trongame.Sensor.Location;

import android.location.Location;

/**
 * Created by Peter on 09/11/2015.
 */
public interface LocationObserver {
    void updateLocation(Location location);
}
