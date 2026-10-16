package app.utils.websocket;

import io.javalin.websocket.WsContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConnectionRegistry {

    // userId -> all open connections for that user (a user can have several tabs open)
    private final Map<Integer, Set<WsContext>> connections = new ConcurrentHashMap<>();

    public void register(int userId, WsContext ctx) {
        connections.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(ctx);
        log.info("WebSocket connected for user {}", userId);
    }

    public void unregister(WsContext ctx) {
        connections.values().forEach(userConnections -> userConnections.remove(ctx));
    }

    public void send(int userId, Object message) {
        Set<WsContext> userConnections = connections.get(userId);
        if (userConnections == null) {
            return; // user is offline, nothing to do
        }

        for (WsContext ctx : userConnections) {
            try {
                synchronized (ctx) {
                    ctx.send(message);
                }
            } catch (Exception e) {
                log.warn("Could not send to a connection for user {}, removing it", userId, e);
                userConnections.remove(ctx);
            }
        }
    }

    public void pingAll() {
        connections.values().forEach(userConnections -> {
            for (WsContext ctx : userConnections) {
                try {
                    synchronized (ctx) {
                        ctx.sendPing();
                    }
                } catch (Exception e) {
                    userConnections.remove(ctx); // dead connection, clean it up
                }
            }
        });
    }
}