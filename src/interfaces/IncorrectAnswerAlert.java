package interfaces;

/**
 * Wrong answer
 */
public class IncorrectAnswerAlert extends LobbyMessage {
    public IncorrectAnswerAlert() {
        this.type = MessageType.INCORRECT_ANSWER;
    }
}
