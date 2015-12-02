package interfaces;

import java.util.Map;

/**
 * Message from server that a turn is ending
 */
public class TurnEndAlert extends LobbyMessage {
    private Map<String, Integer> currentPoints;

    /**
     * @param currentPoints Points of all players at the end of this turn
     */
    public TurnEndAlert(Map<String, Integer> currentPoints) {
        this.type = MessageType.TURN_END;
        this.currentPoints = currentPoints;
    }

    public Map<String, Integer> getCurrentPoints() {
        return currentPoints;
    }
}
