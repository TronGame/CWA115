package cwa115.trongame.Sensor;

import android.hardware.SensorEvent;

import cwa115.trongame.Test.DataQueue;

/**
 * Holds and data from the proximity sensor and evaluates its usefulness.
 */
public class ProximityDataHolder extends SensorDataHolder {

    // Physical constraint:
    private final static float DEFAULT_PROXIMITY_LIMIT = 10f;

    private float proximityLimit;

    public ProximityDataHolder(float proximityLimit){
        //super(1);
        this.proximityLimit = proximityLimit;
    }
    public ProximityDataHolder(){
        this(DEFAULT_PROXIMITY_LIMIT);
    }

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, SensorEvent newSensorEvent) {
        float[] newSensorData = newSensorEvent.values;
        //float proximityLimit = newSensorEvent.sensor.getMaximumRange() / 2;

        if(lastSensorData == null)
            return false;// Do not allow empty last data

        return lastSensorData[0] <= proximityLimit && newSensorData[0] > proximityLimit;
    }

    @Override
    public int getCount() {
        return getUsefulDataCount();
    }
}
