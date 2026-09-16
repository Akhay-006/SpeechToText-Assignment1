/*
 * Tracks server start time and global token usage.
 * Uses thread-safe counters to support concurrent requests safely.
 */
package COMP3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class TokenTrackerService {

    private final Instant serverStart;

    private final AtomicLong inputTokens =
            new AtomicLong(0);

    private final AtomicLong outputTokens =
            new AtomicLong(0);

    public TokenTrackerService() {
        this.serverStart = Instant.now();
    }

    public Instant getServerStart() {
        return serverStart;
    }

    public double getUptimeSeconds() {

        return Duration
                .between(serverStart, Instant.now())
                .toMillis() / 1000.0;
    }
    public long getInputTokens() {
        return inputTokens.get();
    }
    public long getOutputTokens() {
        return outputTokens.get();
    }
    public void addInputTokens(long value) {
        inputTokens.addAndGet(value);
    }
    public void addOutputTokens(long value) {
        outputTokens.addAndGet(value);
    }
    public void addTokenUsage(
            long input,
            long output) {

        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }
}