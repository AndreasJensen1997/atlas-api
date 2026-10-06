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
        System.out.println("--> [AUTH DEBUG] Header received: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("--> [AUTH DEBUG] Failed: Header is null or missing 'Bearer '");
            throw new ApiException(401, "Missing or invalid Authorization header");
        }

        String token = header.substring(7).trim();

        try {
            String email = JWTToken.verifyTokenAndGetSubject(token);
            System.out.println("--> [AUTH DEBUG] Token verified for email: " + email);

            User user = userService.getByEmail(email);
            if (user == null) {
                System.out.println("--> [AUTH DEBUG] Failed: User not found in DB for " + email);
                throw new ApiException(401, "User belonging to token no longer exists");
            }

            ctx.attribute("userId", user.getId());
            System.out.println("--> [AUTH DEBUG] SUCCESS! Set userId attribute to: " + user.getId());

        } catch (Exception e) {
            System.out.println("--> [AUTH DEBUG] Failed token verification: " + e.getMessage());
            throw new ApiException(401, "Invalid token: " + e.getMessage());
        }
    }
}