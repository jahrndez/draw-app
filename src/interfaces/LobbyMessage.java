package interfaces;

import java.io.Serializable;

/**
 * Generic message from the lobby. Serves as the common interface object sent from server, and is demultiplexed into
 * appropriate message type by the client.
 */
public abstract class LobbyMessage implements Serializable {
    protected MessageType type;

    public enum MessageType {
        NEW_USER,
        HOST_CONFIRM,
        GAME_START,
        GAME_END,
        TURN_START,
        TURN_END,
        CORRECT_ANSWER,
        INCORRECT_ANSWER,
        DRAW_INFO,
        PING_RESPONSE
    }

    /**
     * Client reads this to determine which type of message server sent
     * @return LobbyMessage type
     */
    public MessageType type() {
        return type;
    }
}
