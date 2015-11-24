package interfaces;

/**
 * Generic message from the Lobby
 */
public abstract class LobbyMessage {
    private MessageType type;

    public enum MessageType {
        NEW_USER,
        GAME_START
    }

    public MessageType type() {
        return type;
    }
}
