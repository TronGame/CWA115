package cwa115.trongame.GameEvent;

import org.json.JSONObject;

import java.util.ArrayList;

import cwa115.trongame.GameActivity;

/**
 * Created by Peter on 12/11/2015.
 */
public interface GameEvent {
    public JSONObject collectData(GameActivity gameActivity);
    public ArrayList<EventResult> calculateResults(ArrayList<JSONObject> results);
    public int getTime();
    public String getEventType();
    public String getNotification(GameActivity gameActivity);
}
