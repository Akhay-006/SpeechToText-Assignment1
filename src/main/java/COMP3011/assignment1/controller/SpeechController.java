package COMP3011.assignment1.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
/*
 * Handles speech transcription requests.
 * Receives audio files and returns the transcribed text as JSON.
 */
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import COMP3011.assignment1.service.SpeechService;

@RestController
public class SpeechController {

    private final SpeechService speechService;

    public SpeechController(SpeechService speechService) {
        this.speechService = speechService;
    }

    @PostMapping(
            value = "/api/v1/transcriptions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> transcribe(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Upload an audio file."
            );
        }

        String text = speechService.transcribe(file);

        Map<String, String> response =
                new LinkedHashMap<>();

        response.put("text", text);

        return ResponseEntity.ok(response);
    }
}