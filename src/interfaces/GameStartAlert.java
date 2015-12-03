package interfaces;

import java.io.Serializable;

/**
 * Message from the server that the game is about to start
 */
public class GameStartAlert extends LobbyMessage implements Serializable {

    public GameStartAlert() {
        this.type = MessageType.GAME_START;
    }
}
