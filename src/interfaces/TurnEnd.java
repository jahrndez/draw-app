package interfaces;

import java.util.Map;

/**
 * Message from server that a turn is ending
 */
public class TurnEnd extends LobbyMessage {
    private Map<String, Integer> currentPoints;

    /**
     * @param currentPoints Points of all players at the end of this turn
     */
    public TurnEnd(Map<String, Integer> currentPoints) {
        this.type = MessageType.TURN_END;
        this.currentPoints = currentPoints;
    }
}
