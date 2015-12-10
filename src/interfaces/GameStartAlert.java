package interfaces;

import java.io.Serializable;
import java.util.Set;

/**
 * Message from the server that the game is about to start
 */
public class GameStartAlert extends LobbyMessage implements Serializable {
    private Set<String> currentPlayers;

    public GameStartAlert(Set<String> currentPlayers) {
        this.currentPlayers = currentPlayers;
        this.type = MessageType.GAME_START;
    }

    public Set<String> getCurrentPlayers() {
        return currentPlayers;
    }
}
