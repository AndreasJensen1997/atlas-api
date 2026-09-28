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

        String token = header.substring(7);
        String email = JWTToken.verifyTokenAndGetSubject(token);

        // Optional: you can still verify the user exists
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new ApiException(401, "User belonging to token no longer exists");
        }

        // Store the user's ID instead of the detached entity object
        ctx.attribute("currentUserId", user.getUserId());
    }
}