package interfaces;

import java.util.Set;

/**
 * Response from server after the client has requested to join/create a game session
 */
public class CreateJoinResponse {
    private Set<String> existingPlayers;
    private int sessionId;
    private boolean successful;

    /**
     * Failed CreateJoinResponse. Use this constructor to let client know game creation/joining failed
     */
    public CreateJoinResponse() {
        this.successful = false;
    }

    /**
     * Successful CreateJoinResponse. Use this constructor to let client know game creation/joining was successful
     * @param existingPlayers Players already in the game session joined
     * @param sessionId Id of created/joined session
     */
    public CreateJoinResponse(Set<String> existingPlayers, int sessionId) {
        this.existingPlayers = existingPlayers;
        this.sessionId = sessionId;
        this.successful = true;
    }

    public boolean wasSuccessful() {
        return this.successful;
    }

    public Set<String> getExistingPlayers() {
        return this.existingPlayers;
    }

    public int getSessionId() {
        return this.sessionId;
    }
}
