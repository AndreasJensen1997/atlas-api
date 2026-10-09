package app.utils.security;

import app.exceptions.ApiException;
import app.exceptions.TokenCreationException;
import app.utils.general.Utils;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;

public class JWTToken {

    // Must be at least 32 characters (256 bits) long for HS256
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

    public static String generateToken(String email) {
        try {
            // 1. Create HS256 header
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // 2. Set claims (payload: who the token is for, when it expires)
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // 3. Sign the token with your secret
            JWSSigner signer = new MACSigner(SECRET.getBytes());
            signedJWT.sign(signer);

            // 4. Return the serialized token string
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new TokenCreationException("Could not generate token", e);
        }
    }

    public static String verifyTokenAndGetSubject(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());

            // 1. Verify the signature
            if (!signedJWT.verify(verifier)) {
                throw new ApiException(401, "Invalid token signature");
            }

            // 2. Check if the token has expired
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime != null && new Date().after(expirationTime)) {
                throw new ApiException(401, "Token has expired");
            }

            // 3. Return the email (stored in the 'subject')
            return signedJWT.getJWTClaimsSet().getSubject();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(401, "Invalid or malformed token", e);
        }
    }
}