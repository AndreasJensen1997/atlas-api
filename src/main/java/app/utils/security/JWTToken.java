package app.utils.security;

import app.exceptions.ApiException;
import app.exceptions.TokenCreationException;
import app.utils.general.Utils;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.javalin.http.HttpStatus;


import java.util.Date;

public class JWTToken {

    private static final String SECRET = loadSecret();
    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    private static String loadSecret() {
        String secret = System.getenv("DEPLOYED") != null
                ? System.getenv("JWT_SECRET")
                : Utils.getPropertyValue("JWT_SECRET", "config.properties");

        if (secret == null) {
            throw new IllegalStateException("JWT_SECRET is not configured");
        }
        int length = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (length < 32) {
            throw new IllegalStateException("JWT_SECRET is only " + length + " bytes, needs at least 32");
        }
        return secret;
    }

    public static String generateToken(Integer userId) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(userId))
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            JWSSigner signer = new MACSigner(SECRET.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new TokenCreationException("Could not generate token", e);
        }
    }

    public static Integer verifyTokenAndGetUserId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());

            if (!signedJWT.verify(verifier)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime != null && new Date().after(expirationTime)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token has expired");
            }

            return Integer.valueOf(signedJWT.getJWTClaimsSet().getSubject());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or malformed token", e);
        }
    }
}