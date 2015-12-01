package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Represents a game session
 */
public class Session {
    private final int sessionId;
    private SessionState state;
    private Map<Player, Integer> points;
    private Player hostPlayer;
    private Queue<Player> order;  // Player at front of queue is next. After turn, player added to end of queue

    public enum SessionState {
        PREGAME,
        INPROGRESS
    }

    public Session (int sessionId) {
        this.sessionId = sessionId;
        this.state = SessionState.PREGAME;
        this.points = new ConcurrentHashMap<>();
        this.order = new ConcurrentLinkedQueue<>();
    }

    public int getSessionId() {
        return this.sessionId;
    }

    public synchronized Set<String> currentPlayerUsernames() {
        return points.keySet().stream().map(Player::getUsername).collect(Collectors.toSet());
    }

    /**
     * Add a new player to this game session
     * @param socket Socket being used to communicate with this player
     * @param userName Username of new player
     * @param isHostPlayer Whether the player being added is the host. This can only be true once for each session
     * @return True if successful, i.e. the game is taking new players and the player isn't already in this game
     */
    public synchronized boolean addPlayer(Socket socket, String userName, boolean isHostPlayer) {
        if (state != SessionState.PREGAME) {
            return false;
        }

        Player newPlayer = new Player(socket, userName);
        if (points.containsKey(newPlayer)) {
            return false;
        }

        points.put(newPlayer, 0);

        // only set once
        if (isHostPlayer && hostPlayer == null) {
            hostPlayer = newPlayer;
        }

        // TODO: Finish implementation
        // TODO: Remember to broadcast to other clients when new player joins
        return true;
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
    public void start() throws IOException {
        while (numPlayers() < 3) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: finish implementationn
        // Once there are >= 3 players, hostPlayer will send a GameStart object
    }
}
