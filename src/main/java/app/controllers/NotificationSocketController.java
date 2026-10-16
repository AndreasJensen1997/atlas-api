package app.controllers;

import app.services.UserService;
import app.utils.security.JWTToken;
import app.utils.websocket.ConnectionRegistry;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.websocket.WsConnectContext;
import lombok.extern.slf4j.Slf4j;

import static io.javalin.apibuilder.ApiBuilder.ws;

@Slf4j
public class NotificationSocketController implements EndpointGroup {

    private final ConnectionRegistry registry;
    private final UserService userService;

    public NotificationSocketController(ConnectionRegistry registry, UserService userService) {
        this.registry = registry;
        this.userService = userService;
    }

    private void handleConnect(WsConnectContext ctx) {
        String token = ctx.queryParam("token");
        if (token == null || token.isBlank()) {
            ctx.closeSession(4401, "Missing token");
            return;
        }

        try {
            int userId = JWTToken.verifyTokenAndGetUserId(token);
            userService.getById(userId); // make sure the user still exists
            registry.register(userId, ctx);
        } catch (Exception e) {
            log.debug("WebSocket rejected: {}", e.getMessage());
            ctx.closeSession(4401, "Invalid token");
        }
    }

    @Override
    public void addEndpoints() {
        ws("/ws/notifications", ws -> {
            ws.onConnect(this::handleConnect);
            ws.onClose(registry::unregister);
        });
    }
}