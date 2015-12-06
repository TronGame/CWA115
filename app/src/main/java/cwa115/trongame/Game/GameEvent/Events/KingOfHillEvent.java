package cwa115.trongame.Game.GameEvent.Events;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Game.GameUpdateHandler;
import cwa115.trongame.GameActivity;
import cwa115.trongame.Game.GameEvent.EventResult;
import cwa115.trongame.Game.GameEvent.EventUpdateHandler;
import cwa115.trongame.R;

/**
 * Created by Peter on 12/11/2015.
 * The event handling the event where the players need to get to the highest point possible
 */
public class KingOfHillEvent implements GameEvent {
    public static final int TIME = 180;                        // The time the event lasts in seconds
    public static final double[] PRICES = {1000, 300, 200};      // The scores that can be received by the winners

    // elements of the json
    public static final String HEIGHT = "height";
    public static final String EVENT_TYPE = "king_of_hill";

    @Override
    /**
     * @return the amount of time the event takes
     */
    public int getTime() {
        return TIME;
    }

    @Override
    /**
     * @return the type of the event
     */
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    /**
     * @param gameActivity The game activity
     * @return The message that has to be showed on the screen
     */
    public String getNotification(GameActivity gameActivity) {
        return gameActivity.getString(R.string.king_of_hill_text).replaceAll("%time", ""+TIME/60);
    }

    public void startEvent(GameActivity gameActivity) {

    }

    @Override
    /**
     * Collect the data required to calculate the score at the end of the event
     * @param gameActivity The game activity
     * @return JSONObject containing the result
     */
    public JSONObject collectData(GameActivity gameActivity) {
        JSONObject eventMessage = new JSONObject();
        try {
            eventMessage.put(EventUpdateHandler.Protocol.EVENT_TYPE, EVENT_TYPE);
            eventMessage.put(HEIGHT, gameActivity.getHeight());
            eventMessage.put(EventUpdateHandler.Protocol.PLAYER_ID, GameSettings.getPlayerId());
        } catch(JSONException e) {
            // end of the world
        };
        return eventMessage;
    }

    @Override
    /**
     * Calculate the winners of an event from the data collected in collectData
     * @param results A list of JSONObject's containing the results of the event
     * @return A list of the scores stored in EventResults
     */
    public ArrayList<EventResult> calculateResults(ArrayList<JSONObject> results) {
        int maxWinners = PRICES.length;
        ArrayList<Double> winners = new ArrayList<>(Arrays.asList(new Double[maxWinners]));
        ArrayList<Integer> playerIds = new ArrayList<>(Arrays.asList(new Integer[maxWinners]));

        for (JSONObject result: results) {
            try {
                if (result.getString(EventUpdateHandler.Protocol.EVENT_TYPE).equals(EVENT_TYPE)) {
                    double height = result.getDouble(HEIGHT);
                    for (int i=0; i<winners.size(); i++) {
                        if (winners.get(i) == null || height > winners.get(i)) {
                            winners.add(i, height);
                            winners.remove(winners.size() - 1);
                            playerIds.add(i, (int) result.get(GameUpdateHandler.Protocol.PLAYER_ID));
                            playerIds.remove(playerIds.size()-1);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            };
        }
        ArrayList<EventResult> eventResults = new ArrayList<>();
        for (int i=0; i<playerIds.size(); i++) {
            if (playerIds.get(i) != null)
                eventResults.add(new EventResult(playerIds.get(i), PRICES[i]));
        }
        return eventResults;
    }

}
