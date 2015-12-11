package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import interfaces.CreateJoinRequest;
import interfaces.CreateJoinResponse;
import interfaces.PingResponse;

/**
 * Will handle new connections and dispatch into individual sessions (games)
 */
public class Dispatch {
    public static final int LISTENING_PORT = 54777;
    public static final int MAX_NUM_THREADS = 15;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_NUM_THREADS);

        try {
            ServerSocket listeningSocket = new ServerSocket(LISTENING_PORT);
            System.out.println("Server listening on port " + listeningSocket.getLocalPort());
            while (true) {
                Socket socket = listeningSocket.accept();
                threadPool.execute(new HandleClient(socket));
            }
        } catch (Exception e) {
            threadPool.shutdown();
            e.printStackTrace();
        }
    }

    public static class HandleClient implements Runnable {
        private Socket socket;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        public HandleClient(Socket socket) throws IOException {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                CreateJoinRequest createJoinRequest = (CreateJoinRequest) objectInputStream.readObject();

                Session session = null;
                boolean ready = false;
                while (!ready) {
                    switch (createJoinRequest.type()) {
                        case CREATE:
                            session = SessionPool.getSessionPool().createNewSession();
                            ready = true;
                            break;
                        case JOIN:
                            session = SessionPool.getSessionPool().findSessionById(createJoinRequest.getSessionId());
                            ready = true;
                            break;
                        case PING:
                            Map<Integer, Integer> idsToCounts =
                                    SessionPool.getSessionPool().
                                            getAllSessions().
                                            stream().
                                            collect(Collectors.toMap(Session::getSessionId, Session::numPlayers));

                            objectOutputStream.writeObject(new PingResponse(idsToCounts));
                            createJoinRequest = (CreateJoinRequest) objectInputStream.readObject();
                        default:
                            session = null;
                    }
                }

                String username = createJoinRequest.getRequestingClientUserName() 
                		+ " (" + socket.getInetAddress() + ":" + socket.getPort() + ")";
                if (session != null && session.addPlayer(socket,
                                            username,
                                            createJoinRequest.isNewGame())) {
                    // success
                    Set<String> existingPlayers = session.currentPlayerUsernames();
                    objectOutputStream.writeObject(new CreateJoinResponse(existingPlayers, session.getSessionId(), username));
                } else {
                    // failed
                    objectOutputStream.writeObject(new CreateJoinResponse());
                    shutDownResources();
                    return;
                }

                // Only call start on session creation
                if (createJoinRequest.isNewGame())
                    session.start();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                shutDownResources();
            }
        }

        private void shutDownResources() {
            try {
                this.objectOutputStream.close();
                this.objectInputStream.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Exception while closing down resources");
            }
        }
    }
}
