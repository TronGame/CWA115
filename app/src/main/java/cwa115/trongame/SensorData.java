package cwa115.trongame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;

/**
 * Created by Bram on 15-10-2015.
 */
public final class SensorData {

    private final static int DATA_COUNT = 10;
    private final static float PROXIMITY_LIMIT = 10f;

    private static SensorManager mSensorManager;
    private static Sensor LinearAccelerometer, Gyroscope, Proximity;

    private static Queue<Float> proximityData;
    private static Queue<Float[]> gyroscopeData, accelerationData;

    private SensorData(){}

    public static void Initialize(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        LinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        proximityData = new DataQueue<>(DATA_COUNT);
        gyroscopeData = new DataQueue<>(DATA_COUNT);
        accelerationData = new DataQueue<>(DATA_COUNT);

        registerListeners();
    }

    public static int ProximityCount(){
        int count = 0;
        boolean lastClose = false;
        while(proximityData.iterator().hasNext()){
            float nextValue = proximityData.iterator().next();
            if(!lastClose && nextValue <= PROXIMITY_LIMIT){
                lastClose = true;
                count++;
            }else if(lastClose && nextValue > PROXIMITY_LIMIT){
                lastClose = false;
            }
        }
        return count;
    }

    private static void registerListeners(){
        mSensorManager.registerListener(mSensorEventListener, LinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, Proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()){
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    //accelerationData.offer(event.values);
                    break;
                case Sensor.TYPE_GYROSCOPE:

                    break;
                case Sensor.TYPE_PROXIMITY:
                    proximityData.offer(event.values[0]);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
