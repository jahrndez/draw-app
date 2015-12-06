package interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains all session ids and their respective player counts.
 */
public class PingResponse extends LobbyMessage implements Serializable {
    private Map<Integer, Integer> sessionIdsToCounts;

    public PingResponse(Map<Integer, Integer> sessionIdsToCounts) {
        this.sessionIdsToCounts = sessionIdsToCounts;
        this.type = MessageType.PING_RESPONSE;
    }

    /**
     * @return All <session id, # players> pairs
     */
    public Map<Integer, Integer> getSessionIdsToPlayerCounts() {
        return sessionIdsToCounts;
    }
}
