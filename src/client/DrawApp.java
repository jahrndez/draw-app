package client;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import interfaces.CreateJoinRequest;
import interfaces.CreateJoinResponse;

/**
 *
 */
public class DrawApp {

    public static void main(String[] args) {
        Runnable r = () -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // use default
            }
            JFrame f = new JFrame("DrawApp");

            DrawingPane drawApp = new DrawingPane();
            ActionListener create = event -> {
            	try {
	                InetAddress address = InetAddress.getByName("localhost");
	                Socket socket = new Socket(address, 54777);
	                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
	                oos.flush();
	                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	
	                String myUsername = "Alex";
	                CreateJoinRequest request = new CreateJoinRequest(myUsername);
	
	                oos.writeObject(request);
	
	                CreateJoinResponse createJoinResponse = (CreateJoinResponse) ois.readObject();
	
	                if (createJoinResponse.wasSuccessful()) {
	                    System.out.println("Successfully in a game! Game id: " + createJoinResponse.getSessionId());
	                	f.setContentPane(drawApp.getGui());
	                	f.revalidate();
	                	f.repaint();
	                } else {
	                    System.err.println("Failed");
	                    return;
	                }
            	} catch (IOException | ClassNotFoundException e) {
            		e.printStackTrace();
            	}
            };
            ActionListener join = event -> {
            	try {
	                InetAddress address = InetAddress.getByName("128.208.1.139");
	                Socket socket = new Socket(address, 54777);
	                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
	                oos.flush();
	                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	
	                String myUsername = "Alex";
	                CreateJoinRequest request = new CreateJoinRequest(myUsername, 0);
	
	                oos.writeObject(request);
	
	                CreateJoinResponse createJoinResponse = (CreateJoinResponse) ois.readObject();
	
	                if (createJoinResponse.wasSuccessful()) {
	                    System.out.println("Successfully in a game! Game id: " + createJoinResponse.getSessionId());
	                	f.setContentPane(drawApp.getGui());
	                	f.revalidate();
	                	f.repaint();
	                } else {
	                    System.err.println("Failed");
	                    return;
	                }
            	} catch (IOException | ClassNotFoundException e) {
            		e.printStackTrace();
            	}
            };
            MainMenu mainMenu = new MainMenu(create, join);
            
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLocationByPlatform(true);

            f.setContentPane(drawApp.getGui());
            f.pack();
            f.setMinimumSize(f.getSize());
            
            f.setContentPane(mainMenu.getGui());

            f.setVisible(true);
        };

        SwingUtilities.invokeLater(r);
    }
}
