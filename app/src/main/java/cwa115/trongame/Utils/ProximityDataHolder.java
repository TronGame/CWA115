package cwa115.trongame.Utils;

/**
 * Created by Bram on 1-11-2015.
 */
public class ProximityDataHolder extends SensorDataHolder {

    // Physical constraint:
    private final static float PROXIMITY_LIMIT = 10f;

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, float[] newSensorData) {
        if(newSensorData == null) return false;// Do not allow empty new data
        return lastSensorData == null ||
                (lastSensorData[0] > PROXIMITY_LIMIT && newSensorData[0] <= PROXIMITY_LIMIT) ||
                (lastSensorData[0] <= PROXIMITY_LIMIT && newSensorData[0] > PROXIMITY_LIMIT);
        // If lastData is null, newData is good whatever its value
        // If lastData is far away AND newData is close, new data is good
        // If lastData is close AND newData is far, new data is good
    }

    @Override
    public int getCount() {
        return getUsefulDataCount() / 2;
        // Since usefulDataCount is updated every time we come close to the device AND every time
        // we go away, we need to divide it by 2 to get the correct number (coming close and going away counted as 1 'action')
    }
}
