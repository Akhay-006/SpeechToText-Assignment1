package COMP3011.assignment1.service;
import org.springframework.stereotype.Service;

public class TokenTrackerService {
    private final Instant serverStart;

    private final AtomicLong inputTokens;
    private final AtomicLong outputTokens;

    public TokenTrackerService() {

        this.serverStart = Instant.now();

        this.inputTokens = new AtomicLong(0);
        this.outputTokens = new AtomicLong(0);
    }

}
