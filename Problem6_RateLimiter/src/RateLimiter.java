import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class TokenBucket {
    private double tokens;
    private long lastRefillTime;
    private final int maxTokens;
    private final long refillIntervalMs;

    public TokenBucket(int maxTokens, long refillIntervalMs) {
        this.maxTokens = maxTokens;
        this.refillIntervalMs = refillIntervalMs;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refillTokens();

        if (tokens >= 1) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refillTokens() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastRefillTime;
        long intervals = timePassed / refillIntervalMs;

        if (intervals > 0) {
            tokens = Math.min(maxTokens, tokens + intervals);
            lastRefillTime = currentTime;
        }
    }

    public synchronized int getRemainingTokens() {
        refillTokens();
        return (int) tokens;
    }

    public synchronized long getTimeUntilRefill() {
        long timeSinceLastRefill = System.currentTimeMillis() - lastRefillTime;
        return Math.max(0, refillIntervalMs - timeSinceLastRefill);
    }
}

public class RateLimiter {
    private ConcurrentHashMap<String, TokenBucket> clientBuckets;
    private final int maxRequests;
    private final long timeWindowMs;

    public RateLimiter(int maxRequests, long timeWindowMs) {
        this.clientBuckets = new ConcurrentHashMap<>();
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeWindowMs;
    }

    public boolean checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(maxRequests, timeWindowMs));

        return bucket.allowRequest();
    }

    public RateLimitStatus getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) {
            return new RateLimitStatus(maxRequests, maxRequests, 0);
        }

        int remaining = bucket.getRemainingTokens();
        long resetTime = bucket.getTimeUntilRefill();

        return new RateLimitStatus(maxRequests - remaining, maxRequests, resetTime);
    }

    public static class RateLimitStatus {
        public int used;
        public int limit;
        public long resetTimeMs;

        public RateLimitStatus(int used, int limit, long resetTimeMs) {
            this.used = used;
            this.limit = limit;
            this.resetTimeMs = resetTimeMs;
        }

        @Override
        public String toString() {
            return String.format("{used: %d, limit: %d, reset: %dms}",
                    used, limit, resetTimeMs);
        }
    }

    public static void main(String[] args) {
        // Create rate limiter: 10 requests per 5 seconds
        RateLimiter limiter = new RateLimiter(10, 5000);

        String clientId = "abc123";

        System.out.println("Testing Rate Limiter (10 requests per 5 seconds)\n");

        // Make 12 requests
        for (int i = 1; i <= 12; i++) {
            boolean allowed = limiter.checkRateLimit(clientId);
            RateLimiter.RateLimitStatus status = limiter.getRateLimitStatus(clientId);

            System.out.println("Request " + i + ": " + (allowed ? "Allowed" : "Denied") +
                    " - " + status);
        }

        System.out.println("\nWaiting 6 seconds for reset...\n");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("After reset:");
        boolean allowed = limiter.checkRateLimit(clientId);
        System.out.println("Request 13: " + (allowed ? "Allowed" : "Denied") +
                " - " + limiter.getRateLimitStatus(clientId));
    }
}