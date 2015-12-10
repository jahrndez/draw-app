package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import interfaces.CreateJoinRequest;
import interfaces.CreateJoinResponse;
import interfaces.PingResponse;

public class CreateJoinAction implements ActionListener {
	private static String JOIN = "Join Game";
	private static String START = "Start New Game";
	DrawApp game;
	
	public CreateJoinAction(DrawApp gameWindow) {
		game = gameWindow;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
    	try {
            InetAddress address = InetAddress.getByName(game.getServerIP());
            Socket socket = new Socket(address, 54777);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            String myUsername = game.getUsername();
            CreateJoinRequest request = null;
            if (arg0.getActionCommand().equals(JOIN)) {
                CreateJoinRequest ping = new CreateJoinRequest();
                oos.writeObject(ping);
                PingResponse response = (PingResponse) ois.readObject();
                Map<Integer, Integer> games = response.getSessionIdsToPlayerCounts();
            	int id = Integer.parseInt(JOptionPane.showInputDialog("Available Games: \n"
                        + buildAvailableGamesString(games)
                        + "Give Session ID:"));
            	System.out.println("Attempting to join game with id: " + id);
            	request = new CreateJoinRequest(myUsername, id);
            }
            if (arg0.getActionCommand().equals(START))
            	request = new CreateJoinRequest(myUsername);

            oos.writeObject(request);

            CreateJoinResponse createJoinResponse = (CreateJoinResponse) ois.readObject();

            if (createJoinResponse.wasSuccessful()) {
                System.out.println("Successfully in a game! Game id: " + createJoinResponse.getSessionId());
                game.goToLobby(socket, createJoinResponse);
            } else {
                System.err.println("Failed");
                return;
            }
    	} catch (IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
	}

    private String buildAvailableGamesString(Map<Integer, Integer> games) {
        StringBuilder builder = new StringBuilder();
        builder.append("\tID\t# Players\n");
        for (Map.Entry entry : games.entrySet()) {
            builder.append("\t")
                    .append(entry.getKey())
                    .append("\t")
                    .append(entry.getValue())
                    .append("\n");
        }

        return builder.toString();
    }
}
