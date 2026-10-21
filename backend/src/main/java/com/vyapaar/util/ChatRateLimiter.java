package com.vyapaar.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Allows at most `max` calls in any sliding window of `windowMillis`.
 *
 * One shared limiter is enough for a single-owner app. Its job is to cap how much the AI provider
 * can be billed if the public URL is ever found, not to be a per-user quota system.
 */
public class ChatRateLimiter {

    private final int max;
    private final long windowMillis;
    private final Deque<Long> hits = new ArrayDeque<>();

    public ChatRateLimiter(int max, long windowMillis) {
        this.max = max;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        while (!hits.isEmpty() && now - hits.peekFirst() > windowMillis) {
            hits.pollFirst();
        }
        if (hits.size() >= max) {
            return false;
        }
        hits.addLast(now);
        return true;
    }
}
