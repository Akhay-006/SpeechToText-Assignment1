/*
 * Implements the speech transcription service using the OpenAI API.
 * Sends audio for transcription and records the returned token usage.
 */
package COMP3011.assignment1.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OpenAiSpeechService implements SpeechService {

    private static final String API_URL =
            "https://api.openai.com";

    private static final String MODEL_NAME =
            "gpt-4o-mini-transcribe";

    private final RestClient apiClient;
    private final TokenTrackerService tokenTrackerService;

    public OpenAiSpeechService(
            RestClient.Builder restClientBuilder,
            TokenTrackerService tokenTrackerService) {

        this.tokenTrackerService = tokenTrackerService;

        this.apiClient = restClientBuilder
                .baseUrl(API_URL)
                .build();
    }

    @Override
    public String transcribe(MultipartFile audio) throws IOException {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "No audio file was received."
            );
        }

        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY environment variable is not set."
            );
        }

        ByteArrayResource audioResource =
                new ByteArrayResource(audio.getBytes()) {

                    @Override
                    public String getFilename() {

                        String originalFilename =
                                audio.getOriginalFilename();

                        if (originalFilename == null
                                || originalFilename.isBlank()) {

                            return "recording.webm";
                        }

                        return originalFilename;
                    }
                };

        MultipartBodyBuilder body =
                new MultipartBodyBuilder();

        body.part("file", audioResource);

        body.part("model", MODEL_NAME);

        Map<?, ?> response = apiClient
                .post()
                .uri("/v1/audio/transcriptions")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "OpenAI returned an empty response."
            );
        }

        Object text = response.get("text");

        if (text == null) {
            throw new IllegalStateException(
                    "OpenAI did not return transcription text."
            );
        }

        updateTokenUsage(response);

        return text.toString();
    }

    private void updateTokenUsage(Map<?, ?> response) {

        Object usageObject = response.get("usage");

        if (!(usageObject instanceof Map<?, ?> usage)) {
            return;
        }

        Object input = usage.get("input_tokens");
        Object output = usage.get("output_tokens");

        if (input instanceof Number inputTokens) {
            tokenTrackerService.addInputTokens(
                    inputTokens.longValue()
            );
        }

        if (output instanceof Number outputTokens) {
            tokenTrackerService.addOutputTokens(
                    outputTokens.longValue()
            );
        }
    }
}