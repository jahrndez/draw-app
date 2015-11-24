package interfaces;

/**
 * Represents a message from server alerting clients that a new user has joined the session
 */
public class NewUserAlert extends LobbyMessage {
    private String userName;

    public NewUserAlert(String userName) {
        this.userName = userName;
        this.type = MessageType.NEW_USER;
    }

    public String getUserName() {
        return this.userName;
    }
}
