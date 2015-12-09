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
 * Represents a "show off" event, where the player with the largest horizontal acceleration wins.
 */
public class ShowOffEvent implements GameEvent {

    private static final int TIME = 120;
    public static final double[] PRICES = {750, 300, 100};
    private static final String ACCELERATION_FIELD = "acceleration";
    public static final String EVENT_TYPE = "show_off_event";

    @Override
    public int getTime() {
        return TIME;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public void startEvent(GameActivity gameActivity) {
        gameActivity.resetAcceleration();
    }


    @Override
    public String getNotification(GameActivity gameActivity) {
        return gameActivity.getString(R.string.show_off_notification_text).replaceAll("%time", ""+TIME/60);
    }

    @Override
    public JSONObject collectData(GameActivity gameActivity) {
        JSONObject eventMessage = new JSONObject();
        try {
            eventMessage.put(EventUpdateHandler.Protocol.EVENT_TYPE, EVENT_TYPE);
            eventMessage.put(ACCELERATION_FIELD, gameActivity.getAcceleration());
            eventMessage.put(EventUpdateHandler.Protocol.PLAYER_ID, GameSettings.getPlayerId());
        } catch(JSONException e) {
            // end of the world
        }
        return eventMessage;
    }

    @Override
    public ArrayList<EventResult> calculateResults(ArrayList<JSONObject> results) {
        int maxWinners = PRICES.length;
        ArrayList<Double> winners = new ArrayList<>(Arrays.asList(new Double[maxWinners]));
        ArrayList<Integer> playerIds = new ArrayList<>(Arrays.asList(new Integer[maxWinners]));

        for (JSONObject result: results) {
            try {
                if (result.getString(EventUpdateHandler.Protocol.EVENT_TYPE).equals(EVENT_TYPE)) {
                    double acceleration = result.getDouble(ACCELERATION_FIELD);
                    for (int i=0; i< winners.size(); i++) {
                        if((winners.get(i) == null) || (acceleration > winners.get(i))) {
                            winners.add(i, acceleration);
                            winners.remove(winners.size() - 1);
                            playerIds.add(i, (int) result.get(GameUpdateHandler.Protocol.PLAYER_ID));
                            playerIds.remove(playerIds.size()-1);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayList<EventResult> eventResults = new ArrayList<>();
        for (int i=0; i<playerIds.size(); i++) {
            if (playerIds.get(i) != null)
                eventResults.add(new EventResult(playerIds.get(i), PRICES[i]));
        }
        return eventResults;
    }
}
