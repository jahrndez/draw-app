package interfaces;

/**
 * Represents a request for a new game session on the server
 */
public class NewUserRequest {
    private String userName;
    private boolean newGame;

    public NewUserRequest(String userName, boolean newGame) {
        this.userName = userName;
        this.newGame = newGame;
    }

    /**
     * @return Requesting client user name
     */
    public String getRequestingClientUserName() {
        return this.userName;
    }

    /**
     * @return True if this request is for a new game
     */
    public boolean newGame() {
        return this.newGame;
    }
}
