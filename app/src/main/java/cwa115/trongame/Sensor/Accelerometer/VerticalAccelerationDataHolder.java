package cwa115.trongame.Sensor.Accelerometer;

import android.hardware.SensorEvent;

import cwa115.trongame.Sensor.SensorDataHolder;

/**
 * Created by Bram on 5-11-2015.
 */
public class VerticalAccelerationDataHolder extends SensorDataHolder {// VerticalAccelerationDataHolder

    //TODO: Verify physical constraints & isUsefulNewData logic
    // Physical constraints
    private final static float ACCELEROMETER_X_LIMIT = 0.5f;
    private final static float ACCELEROMETER_Z_LIMIT = 1.5f;

    public VerticalAccelerationDataHolder(){
        //super(1);
    }

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, SensorEvent newSensorEvent) {
        if(lastSensorData == null) return true;
        if(newSensorEvent == null || newSensorEvent.values == null) return false;

        float[] newValues = newSensorEvent.values;

        return Math.abs(newValues[0]) > ACCELEROMETER_X_LIMIT && Math.abs(newValues[2]) > ACCELEROMETER_Z_LIMIT;
    }

    @Override
    public int getCount() {
        return getUsefulDataCount();
    }
}
