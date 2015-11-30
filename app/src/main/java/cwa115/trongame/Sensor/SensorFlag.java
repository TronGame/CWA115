package cwa115.trongame.Sensor;

import android.hardware.Sensor;

/**
 * Created by Bram on 1-11-2015.
 */
public enum SensorFlag{// Easy way to refer to 1 or more sensors (using EnumSet)
    NONE(0),
    PROXIMITY(1<<0),
    GYROSCOPE(1<<1),
    ACCELEROMETER(1<<2);

    private final int id;
    SensorFlag(int id) { this.id = id; }
    public int getValue() { return id; }

    /**
     * Retrieve SensorFlag based on corresponding sensorId (SENSOR.TYPE_XXX)
     * @param sensorId The sensorType
     * @return The corresponding SensorFlag
     */
    public static SensorFlag fromSensor(int sensorId){
        switch (sensorId){
            case Sensor.TYPE_PROXIMITY:
                return PROXIMITY;
            case Sensor.TYPE_GYROSCOPE:
                return GYROSCOPE;
            case Sensor.TYPE_ACCELEROMETER:
                return ACCELEROMETER;
            default:
                return NONE;
        }
    }
}