package cwa115.trongame.Sensor.Proximity;

import cwa115.trongame.Sensor.SensorDataObservable;
import cwa115.trongame.Sensor.SensorDataObserver;

/**
 * Created by Bram on 1-11-2015.
 */
public class ProximityObserver implements SensorDataObserver {

    private int countLimit;
    private SensorDataObservable observable;

    public ProximityObserver(SensorDataObservable observable, int countLimit){
        this.countLimit = countLimit;
        this.observable = observable;
    }
    @Override
    public int getCountLimit() {
        return countLimit;
    }

    @Override
    public void updateSensor(SensorDataObservable observable, Object data) {
        if(observable == this.observable){
            int proximityCount = (int)data;// Do something with proximityCount
        }
    }
}
