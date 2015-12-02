package interfaces;

/**
 * Message from server that a new turn is starting
 */
public class TurnStart extends LobbyMessage {
    private String drawerUsername;

    public TurnStart(String drawerUsername) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
    }

    public String getDrawerUsername() {
        return drawerUsername;
    }
}
