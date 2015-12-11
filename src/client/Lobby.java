package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import interfaces.CreateJoinResponse;
import interfaces.GameStart;
import interfaces.GameStartAlert;
import interfaces.LobbyMessage;
import interfaces.NewUserAlert;

public class Lobby implements GameScreen, Runnable {
	private ObjectInputStream in;
	private ObjectOutputStream out;
    private JPanel gui;
    private boolean host;
    private JPanel players;
    private JButton start;
    private Set<String> currentPlayers;
    private String username;
    private Socket socket;
    private DrawApp drawApp;

    public void enterLobby(Socket socket, DrawApp game, CreateJoinResponse join) {  
    	this.socket = socket;
    	this.drawApp = game;
    	currentPlayers = join.getExistingPlayers();
        username = join.username();
    	if(currentPlayers.size() == 1)
    		setAsHost();
    }
    
    public void setAsHost() {
    	host = true;
    	try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void run() {
    	while (true) {
    		try {
    			LobbyMessage message = readObjectFromServer();
    			if (message instanceof NewUserAlert) {
    				NewUserAlert userAdd = (NewUserAlert)message;
	    			currentPlayers.add(userAdd.getUserName());
	    			updatePlayerList();
    			} else if (message instanceof GameStartAlert) {
    				if (out == null)
    					out = new ObjectOutputStream(socket.getOutputStream());
					GameStartAlert gameStartAlert = (GameStartAlert) message;
                    out.flush();
    				drawApp.goToGame(in, out, gameStartAlert.getCurrentPlayers(), username);
    				break;
    			}
    			
				Thread.sleep(50);
			} catch (InterruptedException | ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
    	}
    }
    
    @SuppressWarnings("unchecked")
	public<E> E readObjectFromServer() throws ClassNotFoundException, IOException {
		if (in == null)
        	in = new ObjectInputStream(socket.getInputStream());
		E input = (E) in.readObject();
		
		return input;
    }
    
    public void updatePlayerList() {
    	if (players == null)
    		players = new JPanel(new GridBagLayout());
    	players.removeAll();
    	GridBagConstraints gbc = new GridBagConstraints();

    	gbc.gridy = 0;
    	for (String s : currentPlayers) {
    		JLabel label = new JLabel(s);
    		
    		players.add(label, gbc);
        	gbc.gridy++;
    	}
    	if (start != null)
    		start.setEnabled(currentPlayers.size() >= 3);
    	gui.revalidate();
    }
    
	@Override
	public JComponent getGui() {
		if (gui == null) {
            gui = new JPanel(new GridBagLayout());
        	GridBagConstraints gbc = new GridBagConstraints();
            
            updatePlayerList();
            gui.add(players, gbc);
            
            gbc.gridy = 1;
            if (host) {
            	start = new JButton("Start Game");
        		start.setEnabled(currentPlayers.size() > 3); 
            	start.addActionListener(event -> {
            		System.out.println("Sending command to begin the game!");
            		
            		GameStart start = new GameStart();
            		try {
						out.writeObject(start);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	});
            	
            	gui.add(start, gbc);
            }
        }

        return gui;
    }

}
