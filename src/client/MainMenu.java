package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class MainMenu {
    /** The main GUI that might be added to a frame or applet. */
    private JPanel gui;
    private ActionListener create, join;
    
    public MainMenu(ActionListener onCreateGame, ActionListener onJoinGame) {
    	create = onCreateGame;
    	join = onJoinGame;
    }
    
	public JComponent getGui() {
        if (gui == null) {
            gui = new JPanel(new GridBagLayout());
        	GridBagConstraints gbc = new GridBagConstraints();
            
            JPanel buttons = new JPanel();
        	
            JButton newGame = new JButton();
            newGame.setText("Start New Game");
            newGame.addActionListener(create);
            buttons.add(newGame);

            JButton joinGame = new JButton();
            joinGame.setText("Join Game");
            joinGame.addActionListener(join);
            buttons.add(joinGame);
            
            JButton quit = new JButton();
            quit.setText("Quit Game");
            ActionListener quitGameListener = event -> {
            	System.exit(0);
            };
            quit.addActionListener(quitGameListener);
            buttons.add(quit);
            
            gui.add(buttons, gbc);
        }

        return gui;
    }
}
