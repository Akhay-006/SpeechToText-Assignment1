package COMP3011.assignment1.controller;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import COMP3011.assignment1.service.TokenTrackerService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

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
    @GetMapping(
            value = "/api/v1/admin/uptime",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getUptime() {

        Instant now = Instant.now();

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "utcServerStart",
                tokenTrackerService
                        .getServerStart()
                        .toString()
        );

        response.put(
                "utcNow",
                now.toString()
        );

        response.put(
                "serverUptimeSeconds",
                tokenTrackerService
                        .getUptimeSeconds()
        );

        return ResponseEntity.ok(response);
    }

}
