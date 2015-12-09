package cwa115.trongame.Game.GameEvent.Events;

import org.json.JSONObject;

import java.util.ArrayList;

import cwa115.trongame.GameActivity;
import cwa115.trongame.Game.GameEvent.EventResult;

/**
 * Created by Peter on 12/11/2015.
 */
public interface GameEvent {
    /**
     * @return the amount of time the event takes
     */
    public int getTime();

    /**
     * @return the type of the event
     */
    public String getEventType();

    /**
     * @param gameActivity The game activity
     * @return The message that has to be showed on the screen
     */
    public String getNotification(GameActivity gameActivity);

    public String getEventValue(GameActivity gameActivity, double score);

    /**
     * Setup the event
     * @param gameActivity The game activity
     */
    public void startEvent(GameActivity gameActivity);

    /**
     * Collect the data required to calculate the score at the end of the event
     * @param gameActivity The game activity
     * @return JSONObject containing the result
     */
    public JSONObject collectData(GameActivity gameActivity);

    /**
     * Calculate the winners of an event from the data collected in collectData
     * @param results A list of JSONObject's containing the results of the event
     * @return A list of the scores stored in EventResults
     */
    public ArrayList<EventResult> calculateResults(ArrayList<JSONObject> results);
}
