package interfaces;

import java.io.Serializable;

/**
 * Message from server that a new turn is starting
 */
public class TurnStartAlert extends LobbyMessage implements Serializable {
    private String drawerUsername;
    private String word;
    private long seconds;
    private boolean isDrawer;

    public TurnStartAlert(String drawerUsername, long seconds) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
        this.isDrawer = false;
        this.seconds = seconds;
    }

    public TurnStartAlert(String drawerUsername, String word, long seconds) {
        this.type = MessageType.TURN_START;
        this.drawerUsername = drawerUsername;
        this.word = word;
        this.isDrawer = true;
        this.seconds = seconds;
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

    public long getSeconds() {
        return seconds;
    }
}
