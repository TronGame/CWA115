package cwa115.trongame.Sensor.Accelerometer;

import android.hardware.SensorEvent;

import cwa115.trongame.Sensor.SensorDataHolder;

/**
 * Created by Bram on 1-11-2015.
 */
public class HorizontalAccelerationDataHolder extends SensorDataHolder { // HorizontalAccelerationDataHolder

    //TODO: Verify physical constraints & isUsefulNewData logic
    // Physical constraints
    private final static float ACCELEROMETER_X_LIMIT = 1.5f;
    private final static float ACCELEROMETER_Z_LIMIT = 0.5f;
    private double accelerationMagnitude;

    public HorizontalAccelerationDataHolder(){
        //super(1);
    }

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, SensorEvent newSensorEvent) {
        if(lastSensorData == null) return true;
        if(newSensorEvent == null || newSensorEvent.values == null) return false;

        float[] newValues = newSensorEvent.values;

        // Magnitude
        // Z component also includes gravity but this should be ok
        accelerationMagnitude = Math.sqrt(
                Math.pow(newValues[0], 2) + Math.pow(newValues[1], 2) + Math.pow(newValues[2], 2)
        );
        return (lastSensorData[0] > ACCELEROMETER_X_LIMIT && newValues[0] < -ACCELEROMETER_X_LIMIT && lastSensorData[2] > ACCELEROMETER_Z_LIMIT && newValues[2] < ACCELEROMETER_Z_LIMIT) ||
                (lastSensorData[0] < -ACCELEROMETER_X_LIMIT && newValues[0] > ACCELEROMETER_X_LIMIT && lastSensorData[2] < -ACCELEROMETER_Z_LIMIT && newValues[2] > ACCELEROMETER_Z_LIMIT);
    }

    @Override
    public int getCount() {
        return getUsefulDataCount();
    }

    public double getAccelerationMagnitude() {
        return accelerationMagnitude;
    }


}
