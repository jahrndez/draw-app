package server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a player
 */
public class Player {
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

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    public ObjectInputStream getObjectInputStream() throws IOException {
        if (objectOutputStream == null) {
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        }

        return objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() throws IOException {
        if (objectOutputStream == null) {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
        }

        return objectOutputStream;
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
