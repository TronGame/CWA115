package cwa115.trongame.Utils;

import android.hardware.SensorEvent;

/**
 * Created by Bram on 1-11-2015.
 */
public class GyroscopeDataHolder extends SensorDataHolder {

    // Physical constraints:
    private final static float GYROSCOPE_X_LIMIT = 0.5f;
    private final static float GYROSCOPE_Z_LIMIT = 0.7f;

    @Override
    protected boolean isUsefulNewData(float[] lastSensorData, SensorEvent newSensorEvent) {
        float[] newSensorData = newSensorEvent.values;
        if(newSensorData == null) return false;// Do not allow empty new data
        return lastSensorData == null ||
                (lastSensorData[0] > GYROSCOPE_X_LIMIT && lastSensorData[2] < -GYROSCOPE_Z_LIMIT && newSensorData[0] < -GYROSCOPE_X_LIMIT && newSensorData[2] > GYROSCOPE_Z_LIMIT) ||
                (lastSensorData[0] < -GYROSCOPE_X_LIMIT && lastSensorData[2] > GYROSCOPE_Z_LIMIT && newSensorData[0] > GYROSCOPE_X_LIMIT && newSensorData[2] < -GYROSCOPE_Z_LIMIT);
        // If lastData is null, newData is good whatever its value
        // If lastDataX is above XLimit and lastDataZ beneath ZLimit AND newDataX is beneath XLimit and newDataZ above ZLimit, newData is good
        // If lastDataX is beneath XLimit and lastDataZ above ZLimit AND newDataX is above XLimit and newDataZ beneath ZLimit, newData is good
    }

    @Override
    public int getCount() {
        return getUsefulDataCount() / 2;
        // Again, a usefulDataCount is incremented every time newData 'swaps' with old data, but
        // we want to count the number of turns, so divide it by 2 (2 'swaps' equal 1 turn).
    }
}
