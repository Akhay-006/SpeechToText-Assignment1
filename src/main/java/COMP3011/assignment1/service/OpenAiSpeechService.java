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
    private static final String API_URL = "https://api.openai.com";
    private static final String MODEL_NAME = "gpt-4o-mini-transcribe";

    private final RestClient apiClient;
    private final TokenTrackerService tokenTrackerService;

    public OpenAiSpeechService(TokenTrackerService tokenTrackerService) {
    	this.tokenTrackerService = tokenTrackerService;

        this.apiClient = RestClient.builder()
                .baseUrl(API_URL)
                .build();
    }
    @Override
    public String transcribe(MultipartFile audio) throws IOException {

        String key = System.getenv("OPENAI_API_KEY");

        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "API error."
            );
        }

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "No audio file was received. Record some audio and try again."
            );
        }

        String fileName = audio.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "recording.webm";
        }

        String uploadFileName = fileName;

        ByteArrayResource audioResource =
                new ByteArrayResource(audio.getBytes()) {

            @Override
            public String getFilename() {
                return uploadFileName;
            }
        };

        MultipartBodyBuilder requestBody = new MultipartBodyBuilder();

        requestBody.part("file", audioResource);
        requestBody.part("model", MODEL_NAME);


        Map<?, ?> apiResponse = apiClient
                .post()
                .uri("/v1/audio/transcriptions")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + key
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(requestBody.build())
                .retrieve()
                .body(Map.class);

        if (apiResponse == null) {
            throw new IllegalStateException(
                    "We couldn't get a response from the OpenAi. Please try again."
            );
        }

        Object transcription = apiResponse.get("text");

        if (transcription == null) {
            throw new IllegalStateException(
                    "The audio was processed, but no transcription was returned. Please try again."
            );
        }
        updateTokenUsage(apiResponse);
        return transcription.toString();
    }
    private void updateTokenUsage(Map<?, ?> apiResponse) {

        Object usageObject = apiResponse.get("usage");

        if (!(usageObject instanceof Map<?, ?> usage)) {
            return;
        }

        Object input = usage.get("input_tokens");
        Object output = usage.get("output_tokens");

        if (input instanceof Number inputTokens
                && output instanceof Number outputTokens) {

            tokenTrackerService.addTokenUsage(
                    inputTokens.longValue(),
                    outputTokens.longValue()
            );
        }
    }
}