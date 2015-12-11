package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import interfaces.CreateJoinResponse;

/**
 *
 */
public class DrawApp implements Runnable {
	JFrame gameWindow;
	DrawingPane drawingPane;
	MainMenu mainMenu;
	Lobby lobby;
	
    public static void main(String[] args) {
        Runnable r = new DrawApp();

        SwingUtilities.invokeLater(r);
    }
    
    public void run() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // use default
        }
        gameWindow = new JFrame("DrawApp");
        lobby = new Lobby();
        drawingPane = new DrawingPane();
        mainMenu = new MainMenu(this);
        
        gameWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameWindow.setLocationByPlatform(true);

        gameWindow.setContentPane(drawingPane.getGui());
        gameWindow.pack();
        gameWindow.setMinimumSize(gameWindow.getSize());
        
        gameWindow.setContentPane(mainMenu.getGui());

        gameWindow.setVisible(true);
    }
    
    public void goToGame(ObjectInputStream in, ObjectOutputStream out, Set<String> players, String username) {
    	drawingPane.registerStreams(in, out);
        drawingPane.setBeginningPlayers(players);
        drawingPane.setUsername(username);
    	new Thread(drawingPane).start();
    	gameWindow.setContentPane(drawingPane.getGui());
    	gameWindow.revalidate();
    	gameWindow.repaint();
    }
    
    public void goToLobby(Socket socket, CreateJoinResponse createJoinResponse) {
    	lobby.enterLobby(socket, this, createJoinResponse);
    	new Thread(lobby).start();
    	gameWindow.setContentPane(lobby.getGui());
    	gameWindow.revalidate();
    	gameWindow.repaint();
    }
    
    public String getServerIP() {
    	return mainMenu.getServerIP();
    }
    
    public String getUsername() {
    	return mainMenu.getUsername();
    }
}
