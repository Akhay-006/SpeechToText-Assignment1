package COMP3011.assignment1.controller;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
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
    
    @PostMapping(
            value = "/api/v1/admin/shutdown",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> shutdown() {

        boolean accepted =
                shutdownInProgress.compareAndSet(false, true);

        if (!accepted) {

            Map<String, Object> error =
                    createError(
                            409,
                            "Conflict",
                            "Server shutdown is already in progress.",
                            "/api/v1/admin/shutdown"
                    );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Server shutdown requested."
        );

        CompletableFuture.runAsync(() -> {

            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }

            context.close();
        });

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    private Map<String, Object> createError(
            int status,
            String error,
            String message,
            String path) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                Instant.now().toString()
        );

        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        response.put("path", path);

        return response;
    }

}
