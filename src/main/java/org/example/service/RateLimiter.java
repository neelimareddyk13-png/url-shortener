package org.example.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allow(String clientKey) {
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> new Bucket());
        return bucket.tryConsume();
    }

    private static class Bucket {
        private int count = 0;
        private Instant windowStart = Instant.now();

        synchronized boolean tryConsume() {
            Instant now = Instant.now();
            if (now.getEpochSecond() - windowStart.getEpochSecond() >= WINDOW_SECONDS) {
                windowStart = now;
                count = 0;
            }
            if (count >= MAX_REQUESTS_PER_WINDOW) {
                return false;
            }
            count++;
            return true;
        }
    }
}
