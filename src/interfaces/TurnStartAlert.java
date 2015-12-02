package interfaces;

/**
 * Message from server that a new turn is starting
 */
public class TurnStartAlert extends LobbyMessage {
    private String drawerUsername;

    public TurnStartAlert(String drawerUsername) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
    }

    public String getDrawerUsername() {
        return drawerUsername;
    }
}