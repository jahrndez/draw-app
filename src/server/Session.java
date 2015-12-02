package server;

import interfaces.LobbyMessage;
import interfaces.NewUserAlert;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
     * @param username Username of new player
     * @param isHostPlayer Whether the player being added is the host. This can only be true once for each session
     * @return True if successful, i.e. the game is taking new players and the player isn't already in this game
     */
    public synchronized boolean addPlayer(Socket socket, String username, boolean isHostPlayer) {
        if (state != SessionState.PREGAME) {
            return false;
        }

        Player newPlayer = new Player(socket, username);
        if (points.containsKey(newPlayer)) {
            return false;
        }

        points.put(newPlayer, 0);

        // only set once
        if (isHostPlayer && hostPlayer == null) {
            hostPlayer = newPlayer;
        }

        LobbyMessage newUserAlert = new NewUserAlert(username);
        // alert all existing players that a new player has joined
        for (Player p : points.keySet()) {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(p.getOutputStream());
                objectOutputStream.flush();
                objectOutputStream.writeObject(newUserAlert);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

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
