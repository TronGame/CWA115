package cwa115.trongame;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Queue;

import cwa115.trongame.Test.PlotView;

/**
 * Created by Bram on 15-10-2015.
 */
public final class SensorData {

    private final static int DATA_COUNT = 10;
    private final static float PROXIMITY_LIMIT = 10f;
    private final static float GYROSCOPE_X_LIMIT = 0.5f;
    private final static float GYROSCOPE_Y_LIMIT = 0.7f;

    private static SensorManager mSensorManager;
    private static Sensor LinearAccelerometer, Gyroscope, Proximity;

    private static DataQueue<Float> proximityData;
    private static DataQueue<Float[]> gyroscopeData, accelerationData;

    private SensorData(){}

    public static void Initialize(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        LinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        proximityData = new DataQueue<>(DATA_COUNT);
        gyroscopeData = new DataQueue<>(DATA_COUNT);
        accelerationData = new DataQueue<>(DATA_COUNT);

        Resume();
    }

    public static void Resume(){
        registerListeners();
    }
    public static void Pause(){
        mSensorManager.unregisterListener(mSensorEventListener);
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

    public static int TurningCount(){
        int count = 0;

        while(gyroscopeData.iterator().hasNext()){
            Float[] nextValue = gyroscopeData.iterator().next();
            // TODO: count turnings
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
                    accelerationData.offer(new Float[]{event.values[0], event.values[1], event.values[2]});
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroscopeData.offer(new Float[]{event.values[0], event.values[1], event.values[2]});
                    if(testing){
                        plot.updateDataQueue(gyroscopeQueueId, gyroscopeData, new int[]{0,2});
                        Log.d("SENSORDATA", gyroscopeData.toString());
                    }
                    break;
                case Sensor.TYPE_PROXIMITY:
                    proximityData.offer(event.values[0]);
                    if(testing){
                        plot.updateDataQueue(proximityQueueId, proximityData);
                        Log.d("SENSORDATA", proximityData.toString());
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    // Testing the class:
    private static boolean testing = false;
    private static PlotView plot;
    private static int proximityQueueId;
    private static int[] gyroscopeQueueId;
    public static void Test(Activity a){
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        a.setContentView(R.layout.layout_test);
        plot = (PlotView)a.findViewById(R.id.test_plot);
        testing = true;
        proximityQueueId = plot.addDataQueue(proximityData, Color.BLUE);
        gyroscopeQueueId = plot.addDataQueue(gyroscopeData, new int[]{0,2}, new int[]{Color.RED, Color.GREEN});
    }

}
