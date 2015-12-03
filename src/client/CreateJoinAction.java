package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import interfaces.CreateJoinRequest;
import interfaces.CreateJoinResponse;

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
            	int id = Integer.parseInt(JOptionPane.showInputDialog("Give Session ID:"));
            	System.out.println("Attempting to join game with id: " + id);
            	request = new CreateJoinRequest(myUsername, id);
            }
            if (arg0.getActionCommand().equals(START))
            	request = new CreateJoinRequest(myUsername);

            oos.writeObject(request);

            CreateJoinResponse createJoinResponse = (CreateJoinResponse) ois.readObject();

            if (createJoinResponse.wasSuccessful()) {
                System.out.println("Successfully in a game! Game id: " + createJoinResponse.getSessionId());
                game.goToLobby(createJoinResponse);
            } else {
                System.err.println("Failed");
                return;
            }
    	} catch (IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
	}

}
