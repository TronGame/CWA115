package cwa115.trongame.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bram on 31-10-2015.
 */
public class SensorDataObservable implements SensorEventListener {// Not extending but imitating Observable class

    // SensorManager & used Sensors:
    private SensorManager sensorManager;
    private HashMap<SensorFlag, Sensor> sensors;// All available, used sensors
    private EnumSet<SensorFlag> activeSensors;// All active sensors

    // Stored sensorData
    private HashMap<SensorFlag, SensorDataHolder> sensorData;

    // Observers
    private HashMap<SensorFlag, List<SensorDataObserver>> observers;

    /**
     * Constructor of the SensorDataObservable class. This class listens to sensorChanged events of
     * the current activeSensors (set by the startSensorTracking method) and notifies the listed
     * observers (also set by the startSensorTracking method) when certain predefined limits are
     * exceeded (see SensorDataHolder.getCount() and SensorDataObserver.getCountLimit())
     * @param c A reference to the current context, required to retain the SensorManager service
     */
    public SensorDataObservable(Context c){
        sensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);// Get SensorManager
        activeSensors = EnumSet.noneOf(SensorFlag.class);// Clear activeSensors (set to NONE)

        // Initialize sensorData and SensorDataHolders
        sensorData = new HashMap<>();
        sensorData.put(SensorFlag.PROXIMITY, new ProximityDataHolder());
        sensorData.put(SensorFlag.GYROSCOPE, new GyroscopeDataHolder());
        sensorData.put(SensorFlag.ACCELEROMETER, new AccelerometerDataHolder());

        // Initialize sensors
        sensors = new HashMap<>();
        sensors.put(SensorFlag.PROXIMITY, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        sensors.put(SensorFlag.GYROSCOPE, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.put(SensorFlag.ACCELEROMETER, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

        // Initialize observers
        observers = new HashMap<>();
        observers.put(SensorFlag.PROXIMITY, new ArrayList<SensorDataObserver>());
        observers.put(SensorFlag.GYROSCOPE, new ArrayList<SensorDataObserver>());
        observers.put(SensorFlag.ACCELEROMETER, new ArrayList<SensorDataObserver>());
    }

    /**
     * Resume SensorDataObservable ; restart sensorListening of activeSensors
     */
    public void Resume(){
        for(SensorFlag flag : activeSensors){
            registerListener(flag);
        }
    }

    /**
     * Pause SensorDataObservable ; stop sensorListening and clear sensorData
     */
    public void Pause(){
        unregisterListener();
        for(SensorFlag flag : activeSensors){
            resetSensorData(flag);
        }
    }

    /**
     * Start the tracking of the specified sensor
     * @param flag The sensor to track
     * @param o The observer(s) which will be notified when certain predefined limits are exceeded (see constructor)
     */
    public void startSensorTracking(SensorFlag flag, SensorDataObserver... o){
        observers.get(flag).addAll(Arrays.asList(o));// Add observers

        // Update activeSensors and registerListener
        if(!activeSensors.contains(flag)) {
            activeSensors.add(flag);
            registerListener(flag);
        }
    }

    /**
     * Stop the tracking of the specified sensor by the specified observers. Note that if not all
     * previously added observers are specified, the tracking of the sensor will continue for the
     * remaining observers!
     * @param flag The sensor to which the observers belong
     * @param o The observers which won't be notified anymore
     */
    public void stopSensorTracking(SensorFlag flag, SensorDataObserver... o){
        observers.get(flag).removeAll(Arrays.asList(o));// Remove specified observers

        // If all observers are removed, remove sensor from activeSensors, clear its data and unregister the listener
        if(observers.get(flag).size()==0){
            activeSensors.remove(flag);
            unregisterListener(flag);
            resetSensorData(flag);
        }
    }

    /**
     * Stop the tracking of the specified sensor. All added observers are removed. The sensorData
     * is cleared too.
     * @param flag The sensor for which the tracking will stop
     */
    public void stopSensorTracking(SensorFlag flag){
        observers.get(flag).clear();// Clear the observers
        activeSensors.remove(flag);// Remove sensor from activeSensors
        unregisterListener(flag);// Unregister the listener
        resetSensorData(flag);// Reset the sensor's data
    }

    /**
     * Stop the tracking of all sensors. All observers are removed, all sensorData is cleared.
     */
    public void stopSensorTracking(){
        unregisterListener();// Unregister all listeners

        // Clear observers and reset data of all activeSensors
        for(SensorFlag flag : activeSensors){
            observers.get(flag).clear();
            resetSensorData(flag);
        }
        activeSensors = EnumSet.noneOf(SensorFlag.class);// Clear activeSensors (set to NONE)
    }

    /**
     * Reset the specified sensor's data.
     * @param flag The sensor whose data will be reset
     */
    public void resetSensorData(SensorFlag flag){
        sensorData.get(flag).reset();
    }

    /**
     * SensorDataObservable's onSensorChanged method. This method updates the sensor's data and
     * notifies the registered observers when predefined limits are exceeded (see constructor).
     * @param event The SensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorFlag changedSensor = SensorFlag.fromSensor(event.sensor.getType());// Get changed sensor
        SensorDataHolder changedSensorData = sensorData.get(changedSensor);// Get its data

        changedSensorData.pushNewData(event.values);// Push new data

        for(SensorDataObserver o : observers.get(changedSensor)){
            if(o.getCountLimit()<=changedSensorData.getCount())
                o.update(this, changedSensorData.getCount());// Update observers if limit is exceeded
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Unregister listener of specified sensor.
     * @param flag The specified sensor
     */
    private void unregisterListener(SensorFlag flag){
        sensorManager.unregisterListener(this, sensors.get(flag));
    }

    /**
     * Unregister listeners of all sensors.
     */
    private void unregisterListener(){
        sensorManager.unregisterListener(this);
    }

    /**
     * Register listener for specified sensor.
     * @param flag The specified sensor
     */
    private void registerListener(SensorFlag flag){
        sensorManager.registerListener(this, sensors.get(flag), SensorManager.SENSOR_DELAY_NORMAL);
    }
}
