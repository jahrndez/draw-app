package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton instance that holds all sessions
 */
public class SessionPool {
    private static SessionPool _INSTANCE;

    private Map<Integer, Session> sessions;
    private AtomicInteger counter; // Used for creating unique session IDs

    private SessionPool() {
        sessions = new ConcurrentHashMap<>();
        counter = new AtomicInteger(0);
    }

    public static SessionPool instance() {
        if (_INSTANCE == null) {
            _INSTANCE = new SessionPool();
        }

        return _INSTANCE;
    }

    /**
     * @return Session associated with id if one exists, null otherwise
     */
    public synchronized Session findSessionById(int id) {
        return sessions.get(id);
    }

    /**
     * Create and return new Session object
     * @return Newly created game Session
     */
    public synchronized Session createNewSession() {
        int id = counter.getAndIncrement();
        if (sessions.containsKey(id)) {
            throw new IllegalStateException("Multiple sessions have the same ID");
        }

        sessions.put(id, new Session(id));
        return sessions.get(id);
    }

    /**
     * Removes Session with specified Id. Silently does nothing if Id does not exist
     */
    public synchronized void removeSession(int id) {
        sessions.remove(id);
    }
}
