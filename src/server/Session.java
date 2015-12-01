package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a game session
 */
public class Session {
    private final int sessionId;
    private Map<Player, Integer> points;
    private Player hostPlayer;
    private Queue<Player> order;  // Player at front of queue is next. After turn, player added to end of queue

    public Session (int sessionId) {
        this.sessionId = sessionId;
        this.points = new ConcurrentHashMap<>();
        this.order = new ConcurrentLinkedQueue<>();
    }

    public int getSessionId() {
        return this.sessionId;
    }

    /**
     * Add a new player to this game session
     * @param ipAddress IP address of new player
     * @param socket Socket being used to communicate with this player
     * @param userName Username of new player
     * @param isHostPlayer Whether the player being added is the host. This can only be true once for each session
     * @return True if successful, i.e. the game is taking new players and the player isn't already in this game
     */
    public synchronized boolean addPlayer(InetAddress ipAddress, Socket socket, String userName, boolean isHostPlayer) {
        Player newPlayer = new Player(ipAddress, socket, userName);
        points.put(newPlayer, 0);

        if (isHostPlayer && hostPlayer == null) {
            hostPlayer = newPlayer;
        }

        // TODO: Finish implementation
        // TODO: Remember to broadcast to other clients when new player joins
        return false;
    }

    public synchronized int numPlayers() {
        return points.size();
    }

    public synchronized int getPlayerPoints(Player player) {
        return points.get(player);
    }

    /**
     * Called once per session. Contains logic for game, including turn-taking and direct communication with clients
     */
    public synchronized void start() throws IOException {
        // TODO: implement
        while (points.keySet().size() < 3) {
            // TODO: while there are less than 3 players
        }

        // Once there are >= 3 players, hostPlayer will send a GameStart object
    }
}
