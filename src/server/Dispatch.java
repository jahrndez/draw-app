package server;

import interfaces.CreateJoinRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        public HandleClient(Socket socket) throws IOException {
            this.socket = socket;
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                CreateJoinRequest createJoinRequest = (CreateJoinRequest) objectInputStream.readObject();
                Session session;
                if (createJoinRequest.isNewGame()) {
                    session = SessionPool.instance().createNewSession();
                } else {
                    session = SessionPool.instance().findSessionById(createJoinRequest.getSessionId());
                }

                // Add player info. If request was for new game, make player host (4th param)
                session.addPlayer(socket,
                        createJoinRequest.getRequestingClientUserName(),
                        createJoinRequest.isNewGame());

                // Only call start on session creation
                if (createJoinRequest.isNewGame()) {
                    session.start();
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                shutDownResources();
            }
        }

        private void shutDownResources() {
            try {
                this.objectInputStream.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Exception while closing down resources");
            }
        }
    }
}
