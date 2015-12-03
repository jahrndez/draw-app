package interfaces;

import java.io.Serializable;

/**
 * Generic message from the lobby. Serves as the common interface object sent from server, and is demultiplexed into
 * appropriate message type by the client.
 */
public abstract class LobbyMessage implements Serializable {
    protected MessageType type;

    public enum MessageType {
        NEW_USER,   // alert that a new user has joined the game
        GAME_START,  // we're about to start the game
        TURN_START,  // new turn is starting
        TURN_END,
        DRAW_INFO
    }

    /**
     * Client reads this to determine which type of message server sent
     * @return LobbyMessage type
     */
    public MessageType type() {
        return type;
    }
}
