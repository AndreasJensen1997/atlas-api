package app.utils.security;

import app.entities.User;
import app.exceptions.ApiException;
import app.services.UserService;
import io.javalin.http.Context;

public class SecurityFilter {
    public static void verifyToken(Context ctx, UserService userService) {
        String path = ctx.path();

        if (path.startsWith("/api/auth/")) {
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

            throw new ApiException(401, "Invalid token: " + e.getMessage());
        }
    }
}