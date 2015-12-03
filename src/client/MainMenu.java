package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MainMenu implements GameScreen {
    /** The main GUI that might be added to a frame or applet. */
    private JPanel gui;
    private ActionListener buttonAction;
    private JTextArea nameField, serverField;
    
    public MainMenu(DrawApp game) {
    	buttonAction = new CreateJoinAction(game);
    }
    
	public JComponent getGui() {
        if (gui == null) {
            gui = new JPanel(new GridBagLayout());
        	GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;

            JLabel user = new JLabel("Username: ");
            gui.add(user, gbc);
            nameField = new JTextArea("Anonymous", 1, 23);
            nameField.setMaximumSize(nameField.getPreferredSize());
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gui.add(nameField, gbc);
            
            gbc.gridy = 1;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            
            JLabel server = new JLabel("Server IP: ");
            gui.add(server, gbc);
            
            serverField = new JTextArea("localhost", 1, 23);
            serverField.setMaximumSize(serverField.getPreferredSize());
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gui.add(serverField, gbc);

            gbc.gridy = 2;  
            gbc.gridx = 0;  
            gbc.gridwidth = 1;     
            
            JButton newGame = new JButton();
            newGame.setText("Start New Game");
            newGame.addActionListener(buttonAction);
            gui.add(newGame, gbc);

            JButton joinGame = new JButton();
            joinGame.setText("Join Game");
            joinGame.addActionListener(buttonAction);
            gbc.gridx = 1;
            gui.add(joinGame, gbc);
            
            JButton quit = new JButton();
            quit.setText("Quit Game");
            ActionListener quitGameListener = event -> {
            	System.exit(0);
            };
            quit.addActionListener(quitGameListener);
            gbc.gridx = 2;
            gui.add(quit, gbc);
        }

        return gui;
    }
	
	public String getUsername() {
		return nameField.getText();
	}
	
	public String getServerIP() {
		return serverField.getText();
	}
}
