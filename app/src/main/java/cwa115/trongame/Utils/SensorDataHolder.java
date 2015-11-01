package cwa115.trongame.Utils;

/**
 * Created by Bram on 1-11-2015.
 */
public abstract class SensorDataHolder {

    private float[] lastData;
    private int usefulDataCount;

    public SensorDataHolder(){
        reset();
    }

    /**
     * This method will push the new registered sensorData if it's useful. If isUsefulNewData returns true, usefulDataCount is incremented by 1
     * @param sensorData The new registered sensorData
     * @return The (updated) value of usefulDataCount
     */
    public int pushNewData(float[] sensorData){
        if(isUsefulNewData(lastData, sensorData)){
            usefulDataCount++;
            lastData = sensorData;
        }
        return usefulDataCount;
    }

    /**
     * This method resets the holder; clearing usefulDataCount and setting lastData to an empty float[3]
     */
    public void reset(){
        lastData = new float[3];
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
     * @param newSensorData float[3] containing the new pushed sensorData
     * @return boolean determining whether the newSensorData is useful or not
     */
    protected abstract boolean isUsefulNewData(float[] lastSensorData, float[] newSensorData);

    /**
     * This method should be implemented by inheritors
     * @return This method returns a manipulated value of usefulDataCount (because usefulDataCount is incremented whenever new data is useful, however this may not be desirable behaviour)
     */
    public abstract int getCount();
}
