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
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Queue;

import cwa115.trongame.Test.PlotView;

/**
 * Created by Bram on 15-10-2015.
 */
public final class SensorData {

    private final static int DATA_COUNT = 10;
    private final static float PROXIMITY_LIMIT = 10f;
    private final static float GYROSCOPE_X_LIMIT = 0.5f;
    private final static float GYROSCOPE_Z_LIMIT = 0.7f;

    private static SensorManager mSensorManager;
    private static Sensor LinearAccelerometer, Gyroscope, Proximity;

    private static DataQueue<Float> proximityData;
    private static DataQueue<Float[]> gyroscopeData, accelerationData;
    private static HashMap<SensorFlag, DataQueue> sensorData;

    private static EnumSet<SensorFlag> activeSensors;

    public enum SensorFlag{
        NONE(0),
        PROXIMITY(1<<0),
        GYROSCOPE(1<<1),
        ACCELEROMETER(1<<2);

        private final int id;
        SensorFlag(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    private SensorData(){}

    public static void Initialize(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        LinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        proximityData = new DataQueue<>(DATA_COUNT);
        gyroscopeData = new DataQueue<>(DATA_COUNT);
        accelerationData = new DataQueue<>(DATA_COUNT);
        sensorData = new LinkedHashMap<>();
        sensorData.put(SensorFlag.PROXIMITY, proximityData);
        sensorData.put(SensorFlag.ACCELEROMETER, accelerationData);
        sensorData.put(SensorFlag.GYROSCOPE, gyroscopeData);

        activeSensors = EnumSet.noneOf(SensorFlag.class);

        Resume();
    }

    public static void Resume(){
        registerListeners();
    }
    public static void Pause(){
        unregisterListeners();
    }
    public static EnumSet<SensorFlag> StartSensorTracking(SensorFlag... requestedSensors){
        activeSensors.addAll(Arrays.asList(requestedSensors));
        refreshListeners();
        return activeSensors;
    }
    public static EnumSet<SensorFlag> StopSensorTracking(SensorFlag... sensors){
        for(SensorFlag sensor : sensors) {
            activeSensors.remove(sensor);
            sensorData.get(sensor).clear();
        }
        refreshListeners();
        return activeSensors;
    }

    public static int ProximityCount() throws Exception {
        if(!activeSensors.contains(SensorFlag.PROXIMITY))
            throw new Exception("Proximity data isn't tracked at this moment!");
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

    public static int TurningCount() throws Exception {
        if(!activeSensors.contains(SensorFlag.GYROSCOPE))
            throw new Exception("Gyroscope data isn't tracked at this moment!");
        int count = 0;
        boolean lastXPositiveLastZNegative = false;

        while(gyroscopeData.iterator().hasNext()){
            Float[] nextValue = gyroscopeData.iterator().next();
            if(lastXPositiveLastZNegative && nextValue[0] < -GYROSCOPE_X_LIMIT && nextValue[2] > GYROSCOPE_Z_LIMIT){
                lastXPositiveLastZNegative = false;
                count++;
            }else if(!lastXPositiveLastZNegative && nextValue[0] > GYROSCOPE_X_LIMIT && nextValue[2] < -GYROSCOPE_Z_LIMIT){
                lastXPositiveLastZNegative = true;
            }
        }
        return count;
    }

    private static void registerListeners(){
        if(activeSensors.contains(SensorFlag.ACCELEROMETER))
            mSensorManager.registerListener(mSensorEventListener, LinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if(activeSensors.contains(SensorFlag.GYROSCOPE))
            mSensorManager.registerListener(mSensorEventListener, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        if(activeSensors.contains(SensorFlag.PROXIMITY))
            mSensorManager.registerListener(mSensorEventListener, Proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private static void unregisterListeners(){
        mSensorManager.unregisterListener(mSensorEventListener);
    }
    private static void refreshListeners(){
        unregisterListeners();
        registerListeners();
    }

    private static SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()){
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    accelerationData.offer(new Float[]{event.values[0], event.values[1], event.values[2]});
                    if(testing){
                        plot.updateDataQueue(accelerationQueueId, accelerationData, new int[]{0,1,2});
                        Log.d("SENSORDATA", accelerationData.toString());
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroscopeData.offer(new Float[]{event.values[0], event.values[1], event.values[2]});
                    if(testing){
                        plot.updateDataQueue(gyroscopeQueueId, gyroscopeData, new int[]{0,1,2});
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
    private static int[] accelerationQueueId;
    public static void Test(Activity a){
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        a.setContentView(R.layout.layout_test);
        plot = (PlotView)a.findViewById(R.id.test_plot);
        ((CheckBox)a.findViewById(R.id.proxCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((CheckBox)a.findViewById(R.id.accelCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((CheckBox)a.findViewById(R.id.gyroCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);
        testing = true;
        proximityQueueId = plot.addDataQueue(proximityData, Color.YELLOW);
        gyroscopeQueueId = plot.addDataQueue(gyroscopeData, new int[]{0,1,2}, new int[]{Color.RED, Color.GREEN, Color.BLUE});
        accelerationQueueId = plot.addDataQueue(accelerationData, new int[]{0,1,2}, new int[]{Color.MAGENTA, Color.rgb(255,127,0), Color.CYAN});
        StartSensorTracking(SensorFlag.GYROSCOPE, SensorFlag.PROXIMITY, SensorFlag.ACCELEROMETER);
    }
    private static CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SensorFlag sensor = SensorFlag.NONE;
            switch(buttonView.getId()){
                case R.id.proxCheckBox:
                    sensor = SensorFlag.PROXIMITY;
                    break;
                case R.id.accelCheckBox:
                    sensor = SensorFlag.ACCELEROMETER;
                    break;
                case R.id.gyroCheckBox:
                    sensor = SensorFlag.GYROSCOPE;
                    break;
            }
            if(isChecked)
                StartSensorTracking(sensor);
            else
                StopSensorTracking(sensor);
        }
    };

}
