package server;

import interfaces.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Represents a single game session (single lobby)
 */
public class Session {
    public static final long TURN_LENGTH = 1000 * 60; // millis
    public static final int POINTS_FOR_WIN = 15;

    private final int sessionId;
    private SessionState state;
    private Map<Player, Integer> points;
    private Player hostPlayer;
    private Queue<Player> order;  // Player at front of queue is next. After turn, player added to end of queue

    public enum SessionState {
        PREGAME,
        IN_PROGRESS
    }

    public Session (int sessionId) {
        this.sessionId = sessionId;
        this.state = SessionState.PREGAME;
        this.points = new ConcurrentHashMap<>();
    }

    public int getSessionId() {
        return sessionId;
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
        try {
            // alert all existing players that a new player has joined
            communicateToAllExclude(newUserAlert, newPlayer);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
     * Called once per session, by the thread that created this game session.
     * Contains logic for game, including turn-taking and direct communication with clients.
     */
    public void start() throws IOException {
        while (numPlayers() < 3) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: finish implementation. Currently starts the moment the 3rd player joins, without waiting for host player confirmation

        // Once there are >= 3 players, hostPlayer will send a GameStartAlert object
        LobbyMessage gameStart = new GameStartAlert();
        communicateToAll(gameStart);
        state = SessionState.IN_PROGRESS;

        initializeTurnOrder();

        boolean winnerExists = false; // true when some player hits POINTS_FOR_WIN

        while (!winnerExists) {
            Player currentDrawer = order.remove();
            order.add(currentDrawer);

            LobbyMessage turnStart = new TurnStartAlert(currentDrawer.getUsername());
            communicateToAll(turnStart);

            AtomicBoolean isTurnOver = new AtomicBoolean(false);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isTurnOver.set(true);
                }
            }, TURN_LENGTH);

            // TODO: handle guesses and point accumulation, including setting winnerExists to true when appropriate
            while (!isTurnOver.get()) {
                ObjectInputStream drawerInput = new ObjectInputStream(currentDrawer.getInputStream());
                try {
                    LobbyMessage drawInfo = (LobbyMessage) drawerInput.readObject();
                    if (!(drawInfo instanceof DrawInfo)) {
                        System.out.println("Unexpected LobbyMessage from drawing client. Expected DrawInfo but received "
                                + drawInfo.getClass().getSimpleName());
                        continue;
                    }

                    communicateToAllExclude(drawInfo, currentDrawer);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // Map<Player, Integer>  ->  Map<String, Integer>
            Map<String, Integer> currentPoints =
                    points.
                    entrySet().
                    stream().
                    collect(Collectors.toMap(e -> e.getKey().getUsername(), Map.Entry::getValue));

            LobbyMessage turnEnd = new TurnEndAlert(currentPoints);
            communicateToAll(turnEnd);
        }
    }

    // Sends the specified LobbyMessage to all current players
    private void communicateToAll(LobbyMessage message) throws IOException {
        for (Player p : points.keySet()) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(p.getOutputStream());
            objectOutputStream.flush();
            objectOutputStream.writeObject(message);
        }
    }

    // Sends the specified LobbyMessage to all current players except the player specified
    private void communicateToAllExclude(LobbyMessage message, Player excludedPlayer) throws IOException {
        for (Player p : points.keySet()) {
            if (!p.equals(excludedPlayer)) {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(p.getOutputStream());
                objectOutputStream.flush();
                objectOutputStream.writeObject(message);
            }
        }
    }

    // Initializes player turn order to random order
    private void initializeTurnOrder() {
        LinkedList<Player> players = new LinkedList<>();
        players.addAll(points.keySet());
        Collections.shuffle(players);
        order = players;
    }
}
