package server;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a player
 */
public class Player {
    private InetAddress ipAddress;
    private Socket socket;
    private String username;

    public Player(InetAddress ipAddress, Socket socket, String username) {
        this.ipAddress = ipAddress;
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
