package cwa115.trongame.GameEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Game.GameUpdateHandler;
import cwa115.trongame.GameActivity;
import cwa115.trongame.R;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * Created by Peter on 12/11/2015.
 * The event handling the event where the players need to get to the highest point possible
 */
public class KingOfHillEvent implements GameEvent {
    public static final int TIME = 5*60;  // in seconds
    public static final double[] PRICES = {300, 200, 100};
    public static final String HEIGHT = "height";
    public static final String RESULT = "result";
    public static final String EVENT_TYPE = "king_of_hill";

    @Override
    public int getTime() {
        return TIME;
    }
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public String getNotification(GameActivity gameActivity) {
        // TODO make this less horrible
        return gameActivity.getString(R.string.king_of_hill_text).replaceAll("_time_", ""+TIME);
    }

    @Override
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
    public ArrayList<EventResult> calculateResults(ArrayList<JSONObject> results) {
        int maxWinners = PRICES.length;
        ArrayList<Double> winners = new ArrayList<>(Arrays.asList(new Double[maxWinners]));
        ArrayList<String> playerIds = new ArrayList<>(Arrays.asList(new String[maxWinners]));

        for (JSONObject result: results) {
            try {
                if (result.get(EventUpdateHandler.Protocol.EVENT_TYPE) == EVENT_TYPE) {
                    double height = (double)result.get(HEIGHT);
                    for (int i=0; i< winners.size(); i++) {
                        if (height>winners.get(i)) {
                            winners.add(i, height);
                            winners.remove(winners.size() - 1);
                            playerIds.add(i, (String) result.get(GameUpdateHandler.Protocol.PLAYER_ID));
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
