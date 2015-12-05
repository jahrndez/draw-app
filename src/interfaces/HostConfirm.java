package interfaces;

import java.io.Serializable;

/**
 * Message from server asking host to start game
 */
public class HostConfirm extends LobbyMessage implements Serializable {
	
	public HostConfirm() {
		this.type = MessageType.HOST_CONFIRM;
	}

}
