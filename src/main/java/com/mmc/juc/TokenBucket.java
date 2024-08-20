package com.mmc.juc;

public class TokenBucket implements RateLimiter {
    private final long capacity;
    private final long tokensPerSecond;
    private long tokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, long tokensPerSecond) {
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
    }

    public synchronized boolean tryConsume() {

        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }

        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedTime = now - lastRefillTimestamp;
        long tokensToAdd = (elapsedTime * tokensPerSecond) / 1_000_000_000;
        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}