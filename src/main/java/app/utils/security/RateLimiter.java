package app.utils.security;

import app.exceptions.ApiException;
import io.javalin.http.HttpStatus;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<Integer, Deque<Long>> requests = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowMillis = window.toMillis();
    }

    public void check(int userId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requests.computeIfAbsent(userId, id -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, try again later");
            }
            timestamps.addLast(now);
        }
    }
}