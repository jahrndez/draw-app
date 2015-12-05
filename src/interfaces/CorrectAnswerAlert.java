package interfaces;

import java.io.Serializable;

import interfaces.LobbyMessage.MessageType;

/**
 * Represents a message from server alerting clients who guessed the word correctly
 */

public class CorrectAnswerAlert extends LobbyMessage implements Serializable {
	public CorrectAnswerAlert() {
		this.type = MessageType.CORRECT_ANSWER;
	}
}
