package server;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a game session
 */
public class Session {
    private final int sessionId;
    private Map<InetAddress, String> ipToUsername;
    private Map<InetAddress, Integer> ipToPoints;
    private InetAddress currentDrawer;

    public Session (int sessionId) {
        this.sessionId = sessionId;
        this.ipToUsername = new ConcurrentHashMap<>();
        this.ipToPoints = new ConcurrentHashMap<>();
    }

    public int getSessionId() {
        return this.sessionId;
    }

    /**
     * Add a new player to this game session
     * @param ipAddress IP address of new player
     * @param userName Username of new player
     * @return True if successful, i.e. the game is taking new players and the player isn't already in this game
     */
    public synchronized boolean addPlayer(InetAddress ipAddress, String userName) {
        // TODO: Implement
        return false;
    }

    public synchronized int numPlayers() {
        return ipToUsername.size();
    }

    public synchronized int getPlayerPoints(InetAddress player) {
        return ipToPoints.get(player);
    }

    /**
     * Called once per session
     */
    public synchronized void start() {

    }
}
