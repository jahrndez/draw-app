package server;

import java.net.Socket;

/**
 * Will handle new connections and dispatch into individual sessions (games)
 */
public class Dispatch {
    Runnable r = () -> {
        Socket listeningSocket = new Socket();
    };

}
