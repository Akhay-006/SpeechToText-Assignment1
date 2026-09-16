package COMP3011.assignment1.service;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
@Service
public class TokenTrackerService {
    private final Instant serverStart;
    private final AtomicLong inputTokens;
    private final AtomicLong outputTokens;

    public TokenTrackerService() {

        this.serverStart = Instant.now();

        this.inputTokens = new AtomicLong(0);
        this.outputTokens = new AtomicLong(0);
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
    public void addInputTokens(long input) {
        inputTokens.addAndGet(input);
    }

    public void addOutputTokens(long output) {
        outputTokens.addAndGet(output);
    }
    public void addTokenUsage(long input, long output) {

        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }
}

