package websocket.handler;

import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which WebSocket Sessions belong to which game IDs.
 */

public class WebSocketSessions {
    private final Map<Integer, Set<Session>> sessionMap = new ConcurrentHashMap<>();

    public void addSessionToGame(int gameID, Session session) {
        sessionMap.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSessionFromGame(int gameID, Session session) {
        Set<Session> set = sessionMap.get(gameID);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessionMap.remove(gameID);
            }
        }
    }

    /**
     * Removal from all games (typically onClose or onError)
     */
    public void removeSession(Session session) {
        for (Set<Session> set : sessionMap.values()) {
            set.remove(session);
        }
        sessionMap.values().removeIf(Set::isEmpty);
    }

    public Set<Session> getSessionsForGame(int gameID) {
        return sessionMap.getOrDefault(gameID, Collections.emptySet());
    }

}
