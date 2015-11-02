package cwa115.trongame.Sensor;

/**
 * Created by Bram on 1-11-2015.
 */
public class ProximityObserver implements SensorDataObserver {

    private int countLimit;
    private SensorDataObservable observable;

    public ProximityObserver(SensorDataObservable observable, int countLimit){
        this.countLimit = countLimit;
        this.observable = observable;
    }
    @Override
    public int getCountLimit() {
        return countLimit;
    }

    @Override
    public void update(SensorDataObservable observable, Object data) {
        if(observable == this.observable){
            int proximityCount = (int)data;// Do something with proximityCount
        }
    }
}
