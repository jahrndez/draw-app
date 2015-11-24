package interfaces;

/**
 * Message from the server that the game is about to start
 */
public class GameStart extends LobbyMessage {

    public GameStart() {
        this.type = MessageType.GAME_START;
    }
}
