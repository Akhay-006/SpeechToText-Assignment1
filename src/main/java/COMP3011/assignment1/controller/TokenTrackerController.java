package COMP3011.assignment1.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
/*
 * Provides global speech-to-text usage statistics.
 * Returns the total input and output tokens used since server startup.
 */
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import COMP3011.assignment1.service.TokenTrackerService;

@RestController
public class TokenTrackerController {

    private final TokenTrackerService tokenTrackerService;

    public TokenTrackerController(
            TokenTrackerService tokenTrackerService) {

        this.tokenTrackerService =
                tokenTrackerService;
    }

    @GetMapping(
            value = "/api/v1/global/stats",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>>
            getGlobalStats() {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "inputTokens",
                tokenTrackerService.getInputTokens()
        );

        response.put(
                "outputTokens",
                tokenTrackerService.getOutputTokens()
        );

        return ResponseEntity.ok(response);
    }
}