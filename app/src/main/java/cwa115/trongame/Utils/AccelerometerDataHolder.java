package cwa115.trongame.Utils;

/**
 * Created by Bram on 1-11-2015.
 */
public class AccelerometerDataHolder extends SensorDataHolder {

    //TODO: implement AccelerometerDataHolder

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, float[] newSensorData) {
        return false;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
