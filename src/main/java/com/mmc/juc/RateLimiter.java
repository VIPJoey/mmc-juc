package com.mmc.juc;

public interface RateLimiter {
    boolean tryConsume();
}