package interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Message from server that a turn is ending
 */
public class TurnEndAlert extends LobbyMessage implements Serializable {
    private Map<String, Integer> currentPoints;
    private String word;

    /**
     * @param currentPoints Points of all players at the end of this turn
     */
    public TurnEndAlert(Map<String, Integer> currentPoints, String word) {
        this.type = MessageType.TURN_END;
        this.word = word;
        this.currentPoints = currentPoints;
    }

    public Map<String, Integer> getCurrentPoints() {
        return currentPoints;
    }

    public String getWord() {
        return word;
    }
}
