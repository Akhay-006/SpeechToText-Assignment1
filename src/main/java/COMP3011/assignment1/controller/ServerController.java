package COMP3011.assignment1.controller;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import COMP3011.assignment1.service.TokenTrackerService;

public class ServerController {
    private final TokenTrackerService tokenTrackerService;
    private final ConfigurableApplicationContext context;

    private final AtomicBoolean shutdownInProgress =
            new AtomicBoolean(false);

    public ServerController(
            TokenTrackerService tokenTrackerService,
            ConfigurableApplicationContext context) {

        this.tokenTrackerService = tokenTrackerService;
        this.context = context;
    }

}
