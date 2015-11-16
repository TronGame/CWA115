package cwa115.trongame.GameEvent;

/**
 * Created by Peter on 12/11/2015.
 */
public class EventResult {
    public String playerId;
    public double score;
    public EventResult (String playerId, double score) {
        this.playerId = playerId;
        this.score = score;
    }
}
