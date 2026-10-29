package app.utils.security;

import app.entities.User;
import app.exceptions.ApiException;
import app.services.UserService;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityFilter {

    public static void verifyToken(Context ctx, UserService userService) {
        if (ctx.path().startsWith("/api/v1/auth/")) {
            return;
        }

        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(401, "Missing or invalid Authorization header");
        }
        String token = header.substring(7).trim();

        try {
            Integer userId = JWTToken.verifyTokenAndGetUserId(token);
            User user = userService.getById(userId);

            ctx.attribute("userId", user.getId());
            ctx.attribute("role", user.getRole());

        } catch (Exception e) {
            log.debug("Token rejected: {}", e.getMessage());
            throw new ApiException(401, "Invalid token: " + e.getMessage());
        }
    }
}
