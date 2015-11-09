package cwa115.trongame.Sensor;

import cwa115.trongame.Sensor.SensorDataObservable;

/**
 * Created by Bram on 1-11-2015.
 */
public interface SensorDataObserver {// Not extending, but imitating Observer class

    /**
     * Method notifying the observer when the observable is changed
     * @param observable The observable that has changed
     * @param data Extra data attached by observable
     */
    public void updateSensor(SensorDataObservable observable, Object data);

    /**
     *
     * @return Method returning the countLimit at which this observer should be notified
     */
    public int getCountLimit();
}
