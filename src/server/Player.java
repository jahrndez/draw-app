package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a player
 */
public class Player {
    private InetAddress ipAddress;
    private Socket socket;
    private String username;

    public Player(Socket socket, String username) {
        this.ipAddress = socket.getInetAddress();
        this.socket = socket;
        this.username = username;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getUsername() {
        return this.username;
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public boolean equals(Object o) {
        if (o instanceof Player) {
            Player p = (Player) o;
            return this.ipAddress.equals(p.ipAddress);
        }

        return false;
    }

    public int hashCode() {
        return ipAddress.hashCode();
    }
}
