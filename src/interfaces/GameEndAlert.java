package interfaces;

import java.io.Serializable;
import java.util.List;

/**
 * Message from server that game has ended
 */
public class GameEndAlert extends LobbyMessage implements Serializable {
    public GameEndAlert() {
        this.type = MessageType.GAME_END;
    }
}
