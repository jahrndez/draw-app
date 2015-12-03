package interfaces;

import java.io.Serializable;

/**
 * Message from server that a new turn is starting
 */
public class TurnStartAlert extends LobbyMessage implements Serializable {
    private String drawerUsername;
    private String word;
    private boolean isDrawer;

    public TurnStartAlert(String drawerUsername) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
        this.isDrawer = false;
    }

    public TurnStartAlert(String drawerUsername, String word) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
        this.word = word;
        this.isDrawer = true;
    }

    public boolean isDrawer() {
        return isDrawer;
    }

    public String getWord() {
        return word;
    }

    public String getDrawerUsername() {
        return drawerUsername;
    }
}
