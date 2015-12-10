package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

import javax.swing.*;

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

                JPanel panel = new JPanel();
                panel.add(new JLabel("Please choose a game to join:"));
                JComboBox<String> comboBox = buildComboBox(games);
                panel.add(comboBox);

                int result = JOptionPane.showConfirmDialog(null, panel, "Lobby Selection", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION)
                    return;
                int id = parseSelection((String) comboBox.getSelectedItem());
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

    private JComboBox<String> buildComboBox(Map<Integer, Integer> games) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Map.Entry entry : games.entrySet()) {
            String players = (Integer) entry.getValue() == 1 ? " player" : " players";
            model.addElement("ID: " + entry.getKey() + " | " + entry.getValue() + players);
        }

        return new JComboBox<>(model);
    }

    private int parseSelection(String selection) {
        return Integer.parseInt(selection.split(":")[1].split("\\s+")[1]);
    }
}
