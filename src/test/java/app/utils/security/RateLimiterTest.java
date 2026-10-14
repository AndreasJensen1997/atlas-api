package app.utils.security;

import app.exceptions.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;


public class RateLimiterTest {

    RateLimiter limiter = new RateLimiter(3, Duration.ofSeconds(10));
    RateLimiter shortRateLimiter = new RateLimiter(3, Duration.ofMillis(100));



    @Test
    void check_UnderLimit_Success() {
        limiter.check(1);
        limiter.check(1);
        limiter.check(1);
    }

    @Test
    void check_OverLimit_Throws429() {
        limiter.check(1);
        limiter.check(1);
        limiter.check(1);

        ApiException e = assertThrows(ApiException.class, () -> limiter.check(1));

        assertEquals(429, e.getStatusCode());
    }

    @Test
    void check_OtherUserIsUnaffectedByLimit() {
        // User 1 goes over max limit
        limiter.check(1);
        limiter.check(1);
        limiter.check(1);
        ApiException e = assertThrows(ApiException.class, () -> limiter.check(1));
        assertEquals(429, e.getStatusCode());


        // User 2 begins to use their checks
        limiter.check(2);
        limiter.check(2);
        limiter.check(2);
        assertThrows(ApiException.class, () -> limiter.check(2));
    }


    @Test
    void check_AfterWindowExpires_AllowsAgain() throws InterruptedException {
        // User 1 goes over max limit
        shortRateLimiter.check(1);
        shortRateLimiter.check(1);
        shortRateLimiter.check(1);
        ApiException e = assertThrows(ApiException.class, () -> shortRateLimiter.check(1));
        assertEquals(429, e.getStatusCode());

        // checks that user 1 can call check again after window is closed
        Thread.sleep(150);

        assertDoesNotThrow(() -> shortRateLimiter.check(1));
    }


}
