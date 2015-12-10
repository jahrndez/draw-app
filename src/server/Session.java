package server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import interfaces.*;

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
    private Queue<Player> turnOrder;
    private Queue<Guess> guessQueue;

    public enum SessionState {
        PREGAME,    // before game starts
        IN_PROGRESS,  // game in-progress but between turns
        GUESSING,    // a turn is in progress
        ENDED
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

    public synchronized void shutDownSession() {
        SessionPool.getSessionPool().removeSession(getSessionId());
        // TODO: possibly close each player's stream. What ramifications does that have if a player wants later join a different game?
    }

    /**
     * Called once per session, by the thread that created this game session.
     * Contains logic for game, including turn-taking and direct communication with clients.
     */
    public void start() throws IOException {
        try {
            try {
                Object gameStart = hostPlayer.readFromPlayer();
                if (!(gameStart instanceof GameStart)) {
                    System.out.println("Expected GameStart from host player, instead received " + gameStart.getClass().getSimpleName());
                    return;
                }
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            Set<String> currentPlayers = new HashSet<>();
            points.keySet().stream().forEach(player -> currentPlayers.add(player.getUsername()));
            LobbyMessage gameStartAlert = new GameStartAlert(currentPlayers);
            System.out.println("Start Game");
            communicateToAll(gameStartAlert);
            state = SessionState.IN_PROGRESS;

            initializeTurnOrder();

            boolean winnerExists = false; // true when some player hits POINTS_FOR_WIN

            while (!winnerExists) {
                Player currentDrawer = turnOrder.remove();
                turnOrder.add(currentDrawer);

                state = SessionState.GUESSING;

                String word = WordBank.getWordBank().getNextWord(getSessionId());

                AtomicBoolean isTurnOver = new AtomicBoolean(false);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isTurnOver.set(true);
                        state = SessionState.IN_PROGRESS;
                    }
                }, TURN_LENGTH);

                // Empty out guess queue
                guessQueue = new ConcurrentLinkedQueue<>();

                for (Player player : points.keySet()) {
                    player.setIsDrawer(false);
                }

                // Alert players of turn start
                LobbyMessage turnStart = new TurnStartAlert(currentDrawer.getUsername());
                communicateToAllExclude(turnStart, currentDrawer);
                currentDrawer.setIsDrawer(true);
                // Only provide word to drawer
                LobbyMessage turnStartDrawer = new TurnStartAlert(currentDrawer.getUsername(), word);
                currentDrawer.writeToPlayer(turnStartDrawer);

                // Handle guesses in parallel
                for (Player player : points.keySet()) {
                    (new Thread(new GuessHandler(player, word))).start();
                }

                // Continually transmit drawing information from drawer to guessers
                while (!isTurnOver.get()) {
                    try {
                        LobbyMessage drawInfo = (LobbyMessage) currentDrawer.readFromPlayer();
                        if (!(drawInfo instanceof DrawInfo)) {
                            System.out.println("Unexpected LobbyMessage from drawing client. Expected DrawInfo but received "
                                    + drawInfo.getClass().getSimpleName());
                            continue;
                        }

                        communicateToAllExclude(drawInfo, currentDrawer);
                    } catch (SocketException e) {
                        //TODO: Better fail case for when the host disconnects
                        System.out.println("Host disconnected");
                        e.printStackTrace();
                        return;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                tallyTurnPoints(word, currentDrawer);

                // Map<Player, Integer>  ->  Map<String, Integer>
                Map<String, Integer> currentPoints =
                        points.
                                entrySet().
                                stream().
                                collect(Collectors.toMap(e -> e.getKey().getUsername(), Map.Entry::getValue));

                // Alert all players that the turn has ended and send current points of all players
                LobbyMessage turnEnd = new TurnEndAlert(currentPoints);
                communicateToAll(turnEnd);

                for (Map.Entry entry : points.entrySet()) {
                    if ((Integer) entry.getValue() >= POINTS_FOR_WIN) {
                        winnerExists = true;
                        break;
                    }
                }
            }

            state = SessionState.ENDED;
            communicateToAll(new GameEndAlert());

        } catch (EOFException e) {
            System.out.println("Player quit. Ending game.");
            shutDownSession();
            // TODO: Gracefully handle players exiting
        }
        // TODO: Follow-up: perhaps allow clients to restart current session
    }

    // Process guesses in queue and award points
    private void tallyTurnPoints(String word, Player drawer) {
        int correctGuesses = 0;
        boolean isFirst = true;
        for (Guess guess : guessQueue) {
            if (word.equals(guess.word())) {
                correctGuesses++;
                if (isFirst) {
                    points.put(guess.player(), points.get(guess.player()) + 2);
                    isFirst = false;
                } else {
                    points.put(guess.player(), points.get(guess.player()) + 1);
                }
            }
        }

        // Award points to drawer based on number of players that guessed correctly
        int drawerPoints;
        if (correctGuesses == 0) {
            drawerPoints = 0;
        } else if (correctGuesses == 1) {
            drawerPoints = 2;
        } else {  // > 1
            drawerPoints = 1;
        }

        points.put(drawer, points.get(drawer) + drawerPoints);
    }

    // Sends the specified LobbyMessage to all current players
    private void communicateToAll(LobbyMessage message) throws IOException {
        for (Player p : points.keySet()) {
            p.writeToPlayer(message);
        }
    }

    // Sends the specified LobbyMessage to all current players except the player specified
    private void communicateToAllExclude(LobbyMessage message, Player excludedPlayer) throws IOException {
        for (Player p : points.keySet()) {
            if (!p.equals(excludedPlayer)) {
                p.writeToPlayer(message);
            }
        }
    }

    // Initializes player turn order to random order
    private void initializeTurnOrder() {
        LinkedList<Player> players = new LinkedList<>();
        players.addAll(points.keySet());
        Collections.shuffle(players);
        turnOrder = players;
    }

    /**
     * Allows for handling of guesses from multiple clients in parallel
     */
    public class GuessHandler implements Runnable {
        private Player player;
        private String correctWord;

        public GuessHandler(Player player, String correctWord) {
            this.player = player;
            this.correctWord = correctWord;
        }

        public void run() {
            try {
                boolean done = false;
                while (!player.isDrawer() && state == SessionState.GUESSING && !done) {
                    Object o = player.readFromPlayer();
                    String guess;
                    if (o instanceof String) {
                        guess = (String) o;
                    } else {
                        continue;
                    }
                    guessQueue.add(new Guess(player, guess));
                    System.out.print(player.getUsername() + " guessed \"" + guess + "\", which was ");
                    if (correctWord.equals(guess)) {
                    	System.out.println("correct");
                        done = true;
                        player.writeToPlayer(new CorrectAnswerAlert());
                    } else {
                        System.out.println("incorrect");
                        player.writeToPlayer(new IncorrectAnswerAlert());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
