package cwa115.trongame.Sensor;

import android.hardware.SensorEvent;

/**
 * Created by Bram on 1-11-2015.
 */
public abstract class SensorDataHolder {

    //private DataQueue<float[]> lastData;
    private float[] lastData;
    private int usefulDataCount;

    /*public SensorDataHolder(int dataSize){
        reset();
        lastData = new DataQueue<>(dataSize);
    }*/
    public SensorDataHolder(){
        reset();
    }

    /**
     * This method will push the new registered sensorData if it's useful. If isUsefulNewData returns true, usefulDataCount is incremented by 1
     * @param sensorEvent The new registered sensorEvent
     * @return The (updated) value of usefulDataCount
     */
    public int pushNewData(SensorEvent sensorEvent){
        if(isUsefulNewData(lastData, sensorEvent))
            usefulDataCount++;
        //lastData.offer(sensorEvent.values.clone());
        lastData = sensorEvent.values.clone();
        return usefulDataCount;
    }

    /**
     * This method resets the holder; clearing usefulDataCount and setting lastData to an empty float[3]
     */
    public void reset(){
        //lastData.clear();
        lastData = new float[3];
        clearUsefulDataCount();
    }

    /**
     * This method clears usefulDataCount
     */
    public void clearUsefulDataCount(){
        usefulDataCount = 0;
    }

    /**
     *
     * @return This method returns the current usefulDataCount value
     */
    protected int getUsefulDataCount(){
        return usefulDataCount;
    }

    /**
     * This function should be implemented by inheritors
     * @param lastSensorData float[3] containing the last useful sensorData, can be an empty float!
     * @param newEvent SensorEvent containing the newly pushed sensorData and information
     * @return boolean determining whether the newSensorData is useful or not
     */
    protected abstract boolean isUsefulNewData(float[] lastSensorData, SensorEvent newEvent);
    //protected abstract boolean isUsefulNewData(DataQueue<float[]> lastSensorData, SensorEvent newEvent);

    /**
     * This method should be implemented by inheritors
     * @return This method returns a manipulated value of usefulDataCount (because usefulDataCount is incremented whenever new data is useful, however this may not be desirable behaviour)
     */
    public abstract int getCount();
}
