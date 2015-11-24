package interfaces;

/**
 * Generic message from the Lobby
 */
public abstract class LobbyMessage {
    protected MessageType type;

    public enum MessageType {
        NEW_USER,   // alert that a new user has joined the game
        GAME_START  // we're about to start the game
    }

    public MessageType type() {
        return type;
    }
}
