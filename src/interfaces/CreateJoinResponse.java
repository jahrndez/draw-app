package interfaces;

import java.util.Set;

/**
 * Response from server after the client has requested to join/create a game session
 */
public class CreateJoinResponse {
    private Set<String> existingPlayers;
    private int sessionId;

    public CreateJoinResponse(Set<String> existingPlayers, int sessionId) {
        this.existingPlayers = existingPlayers;
        this.sessionId = sessionId;
    }

    public Set<String> getExistingPlayers() {
        return this.existingPlayers;
    }

    public int getSessionId() {
        return this.sessionId;
    }
}
