package interfaces;

import java.io.Serializable;
import java.util.Set;

/**
 * Response from server after the client has requested to join/create a game session
 */
public class CreateJoinResponse implements Serializable {
    private Set<String> existingPlayers;
    private int sessionId;
    private String username;
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
     * @param username Username assigned to this player by the server
     */
    public CreateJoinResponse(Set<String> existingPlayers, int sessionId, String username) {
        this.existingPlayers = existingPlayers;
        this.sessionId = sessionId;
        this.username = username;
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

    public String username() {
        return username;
    }
}
