package server;

import interfaces.LobbyMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a player
 */
public class Player {
	private boolean drawer;
    private InetAddress ipAddress;
    private Socket socket;
    private String username;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Player(Socket socket, String username) {
        this.ipAddress = socket.getInetAddress();
        this.socket = socket;
        this.username = username;
    }
    
    public void setIsDrawer(boolean drawing) {
    	drawer = drawing;
    }
    
    public boolean isDrawer() {
    	return drawer;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Write the specified message to this Player. Thread-safe so no 2 writes are made concurrently.
     * @param message LobbyMessage to be sent
     */
    public synchronized void writeToPlayer(LobbyMessage message) throws IOException {
        if (objectOutputStream == null) {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
        }

        objectOutputStream.flush();
        objectOutputStream.writeObject(message);
    }

    /**
     * Read general object from this Player's object input stream.
     * @return Object sent by the client
     */
    public Object readFromPlayer() throws IOException, ClassNotFoundException {
        if (objectInputStream == null) {
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
//            System.out.println("Creating input stream for player: " + username);
        }
        
        return objectInputStream.readObject();
    }

    public void shutDownConnections() throws IOException {
        objectInputStream.close();
        objectOutputStream.close();
        socket.close();
    }

    public boolean equals(Object o) {
        if (o instanceof Player) {
            Player p = (Player) o;
            return this.ipAddress.equals(p.ipAddress) && this.socket.getPort() == p.getSocket().getPort();
        }

        return false;
    }

    public int hashCode() {
        return (ipAddress.toString() + socket.getPort()).hashCode();
    }
}
