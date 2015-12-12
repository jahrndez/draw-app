package interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Message from server that game has ended
 */
public class GameEndAlert extends LobbyMessage implements Serializable {
    private Set<String> winners;

    public GameEndAlert(Set<String> winners) {
        this.winners = winners;
        this.type = MessageType.GAME_END;
    }

    public Set<String> getWinners() {
        return winners;
    }
}
