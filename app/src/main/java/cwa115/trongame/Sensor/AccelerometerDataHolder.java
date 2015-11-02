package cwa115.trongame.Sensor;

import android.hardware.SensorEvent;

import cwa115.trongame.Sensor.SensorDataHolder;

/**
 * Created by Bram on 1-11-2015.
 */
public class AccelerometerDataHolder extends SensorDataHolder {

    //TODO: implement AccelerometerDataHolder

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, SensorEvent newSensorEvent) {
        return false;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
