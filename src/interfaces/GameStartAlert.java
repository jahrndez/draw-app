package interfaces;

/**
 * Message from the server that the game is about to start
 */
public class GameStartAlert extends LobbyMessage {

    public GameStartAlert() {
        this.type = MessageType.GAME_START;
    }
}
